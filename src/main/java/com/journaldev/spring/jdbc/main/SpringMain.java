package com.journaldev.spring.jdbc.main;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;



import com.journaldev.spring.jdbc.dao.UrlDAO;
import com.journaldev.spring.jdbc.model.Url;

public class SpringMain {

	public static void main(String[] args) throws IOException {
		//Get the Spring Context
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");
		//Get the UrlDAO Bean
		UrlDAO urlDAO = ctx.getBean("urlDAO", UrlDAO.class);

		Url pharma = new Url();
		urlDAO.runSql2("TRUNCATE Recrd;");    
		urlDAO.processPage("Grande Pharmacie d'Agadir","av. Mly Abdallah , imm. M1 Agadir","Tél.: 05 28 84 79 52",false);
		Document doc = Jsoup.connect("http://www.anahna.com/pharmacies-agadir-ca7-qa0.html").timeout(10*10000).get();
		Elements newsHeadlines = doc.select("h1");
		Elements questions = doc.select("div .right").select("p:eq(1)");
		Elements tels = doc.select("div.right").select("p:eq(2)");
		for (int i = 1; i < 53; i++) {
			Element elem = newsHeadlines.get(i);
			Element adress = questions.get(i-1);
			Element tele = tels.get(i-1);

			boolean b= urlDAO.isPharmaGarde(elem.text());
			if(b==true){
				System.out.println(adress.text()+tele.text()+elem.text());
				pharma.setRecordID(i);
				pharma.setGarde(true);
				urlDAO.update(pharma);
			}
		}
		
		ctx.close();

		System.out.println("DONE");
	}
}