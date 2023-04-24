package org.pdks.session;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.pdks.entity.HareketKGS;
import org.pdks.entity.KapiKGS;
import org.pdks.entity.KapiView;
import org.pdks.entity.Personel;
import org.pdks.entity.PersonelHareketIslem;
import org.pdks.entity.PersonelIzin;
import org.pdks.entity.PersonelView;
import org.pdks.entity.Sirket;
import org.pdks.entity.Vardiya;
import org.pdks.entity.VardiyaGun;
import org.pdks.security.entity.User;

@Name("calismaSaatleriHome")
public class CalismaSaatleriHome extends EntityHome<VardiyaGun> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5596157513342417469L;
	static Logger logger = Logger.getLogger(CalismaSaatleriHome.class);

	@RequestParameter
	Long kgsHareketId;
	@In(create = true)
	PdksEntityController pdksEntityController;
	@In(required = false)
	User authenticatedUser;
	@In(required = false, create = true)
	OrtakIslemler ortakIslemler;
	@In(required = false, create = true)
	EntityManager entityManager;
	@In(required = false, create = true)
	List<User> userList;

	@In(required = false, create = true)
	HashMap parameterMap;

	List<HareketKGS> hareketList = new ArrayList<HareketKGS>();
	List<VardiyaGun> vardiyaGunList = new ArrayList<VardiyaGun>();

	private String islemTipi, bolumAciklama;
	private Date date;
	private Session session;

	@In(required = false)
	FacesMessages facesMessages;

	@Override
	public Object getId() {
		if (kgsHareketId == null) {
			return super.getId();
		} else {
			return kgsHareketId;
		}
	}

	@Override
	@Begin(join = true)
	public void create() {
		super.create();
	}

	@Begin(join = true, flushMode = FlushModeType.MANUAL)
	public void sayfaGirisAction() {
		if (session == null)
			session = PdksUtil.getSessionUser(entityManager, authenticatedUser);
		session.setFlushMode(FlushMode.MANUAL);
		session.clear();
		setHareketList(new ArrayList<HareketKGS>());
		setVardiyaGunList(new ArrayList<VardiyaGun>());
		HareketKGS hareket = new HareketKGS();
		hareket.setPersonel(new PersonelView());
		hareket.setKapiView(new KapiView());
		hareket.setIslem(new PersonelHareketIslem());
		setDate(new Date());
		fillEkSahaTanim();
	}

	private void fillEkSahaTanim() {
		HashMap sonucMap = ortakIslemler.fillEkSahaTanim(session, Boolean.FALSE, null);
		bolumAciklama = (String) sonucMap.get("bolumAciklama");
	}

	public void hareketGoster(VardiyaGun pdksVardiyaGun) {
		setInstance(pdksVardiyaGun);
		List<HareketKGS> kgsList = pdksVardiyaGun.getHareketler();
		setHareketList(kgsList);

	}

	public void fillHareketList() throws Exception {
		String yemekHaketmeStr = (parameterMap.containsKey("yemekHaketmeSuresi") ? (String) parameterMap.get("yemekHaketmeSuresi") : "4800");
		String yemekSuresiStr = (parameterMap.containsKey("yemekSuresi") ? (String) parameterMap.get("yemekSuresi") : "3000");
		double yemekHaketmedakika = Double.parseDouble(yemekHaketmeStr);
		double yemekSuresiDakika = Double.parseDouble(yemekSuresiStr);
		List<VardiyaGun> vardiyaList = new ArrayList<VardiyaGun>();
		List<PersonelIzin> izinList = new ArrayList<PersonelIzin>();
		List<HareketKGS> kgsList = new ArrayList<HareketKGS>();
		List<Long> perIdList = new ArrayList<Long>();
		List<Personel> tumPersoneller = new ArrayList<Personel>(authenticatedUser.getTumPersoneller());
		for (Iterator iterator = tumPersoneller.iterator(); iterator.hasNext();) {
			Personel per = (Personel) iterator.next();
			if (per.getSirket().isPdksMi())
				perIdList.add(per.getId());
			else
				iterator.remove();
		}
		List<Integer> izinDurumList = new ArrayList<Integer>();
		// izinDurumList.add(PersonelIzin.IZIN_DURUMU_BIRINCI_YONETICI_ONAYINDA);
		izinDurumList.add(PersonelIzin.IZIN_DURUMU_REDEDILDI);
		izinDurumList.add(PersonelIzin.IZIN_DURUMU_SISTEM_IPTAL);
		HashMap parametreMap2 = new HashMap();
		parametreMap2.put("baslangicZamani<=", PdksUtil.tariheGunEkleCikar(date, 1));
		parametreMap2.put("bitisZamani>=", PdksUtil.tariheGunEkleCikar(date, -1));
		parametreMap2.put("izinTipi.bakiyeIzinTipi=", null);
		parametreMap2.put("izinSahibi.id", perIdList);
		if (izinDurumList.size() > 1)
			parametreMap2.put("izinDurumu not ", izinDurumList);
		else
			parametreMap2.put("izinDurumu <> ", PersonelIzin.IZIN_DURUMU_REDEDILDI);
		izinList = pdksEntityController.getObjectByInnerObjectListInLogic(parametreMap2, PersonelIzin.class);
		TreeMap<Long, List<PersonelIzin>> izinMap = new TreeMap<Long, List<PersonelIzin>>();
		for (PersonelIzin izin : izinList) {
			Long id = izin.getIzinSahibi().getId();
			List<PersonelIzin> list = izinMap.containsKey(id) ? izinMap.get(id) : new ArrayList<PersonelIzin>();
			if (list.isEmpty())
				izinMap.put(id, list);
			list.add(izin);
		}

		TreeMap<String, VardiyaGun> vardiyaMap = ortakIslemler.getIslemVardiyalar(tumPersoneller, date, PdksUtil.tariheGunEkleCikar(date, 1), Boolean.FALSE, session, Boolean.TRUE);
		vardiyaList = new ArrayList<VardiyaGun>(vardiyaMap.values());
		if (session != null)
			parametreMap2.put(PdksEntityController.MAP_KEY_SESSION, session);
		Date tarih1 = null;
		Date tarih2 = null;
		Date tarih3 = null;
		Date tarih4 = null;
		tumPersoneller = null;
		perIdList = null;
		for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
			VardiyaGun pdksVardiyaGun = (VardiyaGun) iterator.next();
			if (PdksUtil.tarihKarsilastirNumeric(pdksVardiyaGun.getVardiyaDate(), date) != 0) {
				iterator.remove();
				continue;

			}
			if ((tarih1 == null && tarih3 == null) || pdksVardiyaGun.getIslemVardiya().getVardiyaBasZaman().getTime() < tarih3.getTime()) {
				tarih3 = pdksVardiyaGun.getIslemVardiya().getVardiyaBasZaman();
				tarih1 = pdksVardiyaGun.getIslemVardiya().getVardiyaFazlaMesaiBasZaman();

			}

			if (tarih2 == null || pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime() > tarih4.getTime()) {
				tarih4 = pdksVardiyaGun.getIslemVardiya().getVardiyaBitZaman();
				tarih2 = pdksVardiyaGun.getIslemVardiya().getVardiyaFazlaMesaiBitZaman();

			}

		}
		List<Long> kapiIdler = ortakIslemler.getPdksKapiIdler(session, Boolean.TRUE);

		if (kapiIdler != null && !kapiIdler.isEmpty())
			kgsList = ortakIslemler.getPdksHareketBilgileri(Boolean.TRUE, kapiIdler, tumPersoneller, tarih1, tarih2, HareketKGS.class, session);
		else
			kgsList = new ArrayList<HareketKGS>();
		if (!kgsList.isEmpty())
			kgsList = PdksUtil.sortListByAlanAdi(kgsList, "zaman", Boolean.FALSE);
		TreeMap<Long, List<HareketKGS>> hareketMap = new TreeMap<Long, List<HareketKGS>>();
		for (HareketKGS hareketKGS : kgsList) {
			Personel personel = hareketKGS.getPersonel() != null ? hareketKGS.getPersonel().getPdksPersonel() : null;
			if (personel != null) {
				List<HareketKGS> list = hareketMap.containsKey(personel.getId()) ? hareketMap.get(personel.getId()) : new ArrayList<HareketKGS>();
				if (list.isEmpty())
					hareketMap.put(personel.getId(), list);
				list.add(hareketKGS);
			}
		}

		try {

			for (Iterator iterator = vardiyaList.iterator(); iterator.hasNext();) {
				VardiyaGun vardiyaGun = (VardiyaGun) iterator.next();
				Long personelId = vardiyaGun.getPersonel().getId();
				Vardiya islemVardiya = vardiyaGun.getIslemVardiya();
				vardiyaGun.setHareketler(null);
				vardiyaGun.setGirisHareketleri(null);
				vardiyaGun.setCikisHareketleri(null);
				if (hareketMap.containsKey(personelId)) {
					List<HareketKGS> list = hareketMap.get(personelId);
					for (Iterator iterator1 = list.iterator(); iterator1.hasNext();) {
						HareketKGS kgsHareket = (HareketKGS) iterator1.next();
						if (kgsHareket.getZaman().getTime() >= islemVardiya.getVardiyaFazlaMesaiBasZaman().getTime() && kgsHareket.getZaman().getTime() <= islemVardiya.getVardiyaFazlaMesaiBitZaman().getTime())
							vardiyaGun.addPersonelHareket(kgsHareket);
						iterator1.remove();
					}
				}

				if (!vardiyaGun.isHareketHatali() && vardiyaGun.getGirisHareketleri() == null) {
					if (izinMap.containsKey(personelId)) {
						List<PersonelIzin> list = izinMap.get(personelId);
						for (Iterator iterator2 = list.iterator(); iterator2.hasNext();) {
							PersonelIzin personelIzin = (PersonelIzin) iterator2.next();

							if (personelIzin.getBitisZamani().getTime() >= vardiyaGun.getIslemVardiya().getVardiyaBasZaman().getTime() && personelIzin.getBaslangicZamani().getTime() <= vardiyaGun.getIslemVardiya().getVardiyaBitZaman().getTime()) {

								vardiyaGun.setIzin(personelIzin);
								iterator2.remove();
								break;
							}

						}
					}
				}

				double calismaSuresi = 0;
				if (!vardiyaGun.isHareketHatali() && vardiyaGun.getGirisHareketleri() != null) {

					for (int i = 0; i < vardiyaGun.getGirisHareketleri().size(); i++) {
						calismaSuresi += PdksUtil.getDakikaFarki(vardiyaGun.getCikisHareketleri().get(i).getZaman(), vardiyaGun.getGirisHareketleri().get(i).getZaman());

					}
					if (calismaSuresi > yemekHaketmedakika) {
						calismaSuresi = calismaSuresi - yemekSuresiDakika;
					}

				}
				int yarimYuvarla = vardiyaGun.getYarimYuvarla();
				vardiyaGun.setCalismaSuresi(PdksUtil.setSureDoubleTypeRounded(calismaSuresi / 60.0d, yarimYuvarla));

			}

		} catch (Exception e) {
			logger.error("PDKS hata in : \n");
			e.printStackTrace();
			logger.error("PDKS hata out : " + e.getMessage());

		}
		setVardiyaGunList(vardiyaList);

	}

	public String calismaSaatleriExcel() {
		try {

			ByteArrayOutputStream baosDosya = calismaSaatleriExcelDevam();
			if (baosDosya != null) {
				String dosyaAdi = "ÇalışmaSaatleri_" + PdksUtil.convertToDateString(date, "yyyyMM") + ".xlsx";
				PdksUtil.setExcelHttpServletResponse(baosDosya, dosyaAdi);
			}
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());

		}

		return "";
	}

	public ByteArrayOutputStream calismaSaatleriExcelDevam() {
		ByteArrayOutputStream baos = null;
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = ExcelUtil.createSheet(wb, "Hareket Durumu Listesi", false);
		CellStyle style = ExcelUtil.getStyleData(wb);
		CellStyle styleCenter = ExcelUtil.getStyleData(wb);
		styleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		CellStyle header = ExcelUtil.getStyleHeader(wb);
		int row = 0;
		int col = 0;
		boolean tesisDurum = ortakIslemler.getListTesisDurum(vardiyaGunList);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.personelNoAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Personel");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.yoneticiAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.sirketAciklama());
		if (tesisDurum)
			ExcelUtil.getCell(sheet, row, col++, header).setCellValue(ortakIslemler.tesisAciklama());
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue(bolumAciklama);
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Çalışma Süresi");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Vardiya");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("İlk Giriş");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Son Çıkış");
		ExcelUtil.getCell(sheet, row, col++, header).setCellValue("Hareketler");

		for (VardiyaGun calismaPlani : vardiyaGunList) {
			row++;
			col = 0;
			Personel personel = calismaPlani.getPersonel();
			Sirket sirket = personel.getSirket();
			Vardiya vardiya = calismaPlani.getVardiya();
			ExcelUtil.getCell(sheet, row, col++, styleCenter).setCellValue(personel.getPdksSicilNo());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getAdSoyad());
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getYoneticisi() != null && personel.getYoneticisi().isCalisiyorGun(date) ? personel.getYoneticisi().getAdSoyad() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sirket.getAd());
			if (tesisDurum)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getTesis() != null ? personel.getTesis().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(personel.getEkSaha3() != null ? personel.getEkSaha3().getAciklama() : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.isCalisma() && calismaPlani.getCalismaSuresi() > 0.0d ? authenticatedUser.sayiFormatliGoster(calismaPlani.getCalismaSuresi()) + " saat" : "");
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(vardiya.getAciklama());
			if (calismaPlani.getGirisHareket() != null)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(authenticatedUser.dateTimeFormatla(calismaPlani.getGirisHareket().getZaman()));
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			if (calismaPlani.getCikisHareket() != null)
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue(authenticatedUser.dateTimeFormatla(calismaPlani.getCikisHareket().getZaman()));
			else
				ExcelUtil.getCell(sheet, row, col++, style).setCellValue("");
			StringBuffer sb = new StringBuffer();
			if (calismaPlani.getHareketler() != null) {
				for (Iterator iterator = calismaPlani.getHareketler().iterator(); iterator.hasNext();) {
					HareketKGS hareketKGS = (HareketKGS) iterator.next();
					KapiKGS kapiKGS = hareketKGS.getKapiKGS();
					sb.append((kapiKGS.getKapi() != null ? kapiKGS.getKapi().getAciklama() : kapiKGS.getAciklamaKGS()) + " --> " + authenticatedUser.dateTimeFormatla(hareketKGS.getZaman()) + (iterator.hasNext() ? "\n" : ""));
				}
			} else if (calismaPlani.getIzin() != null) {
				PersonelIzin izin = calismaPlani.getIzin();
				sb.append(izin.getIzinTipi().getIzinTipiTanim().getAciklama() + "\n");
				sb.append(authenticatedUser.dateFormatla(izin.getBaslangicZamani()) + " - ");
				sb.append(authenticatedUser.dateFormatla(izin.getBitisZamani()));

			}
			ExcelUtil.getCell(sheet, row, col++, style).setCellValue(sb.toString());
		}
		try {

			for (int i = 0; i < col; i++)
				sheet.autoSizeColumn(i);
			baos = new ByteArrayOutputStream();
			wb.write(baos);
		} catch (Exception e) {
			logger.error("Pdks hata in : \n");
			e.printStackTrace();
			logger.error("Pdks hata out : " + e.getMessage());
			baos = null;
		}
		return baos;
	}

	public List<HareketKGS> getHareketList() {
		return hareketList;
	}

	public void setHareketList(List<HareketKGS> hareketList) {
		this.hareketList = hareketList;
	}

	public String getIslemTipi() {
		return islemTipi;
	}

	public void setIslemTipi(String islemTipi) {
		this.islemTipi = islemTipi;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<VardiyaGun> getVardiyaGunList() {
		return vardiyaGunList;
	}

	public void setVardiyaGunList(List<VardiyaGun> vardiyaGunList) {
		this.vardiyaGunList = vardiyaGunList;
	}

	public String getBolumAciklama() {
		return bolumAciklama;
	}

	public void setBolumAciklama(String bolumAciklama) {
		this.bolumAciklama = bolumAciklama;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
