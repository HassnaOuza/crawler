package com.journaldev.spring.jdbc.dao;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import com.journaldev.spring.jdbc.model.Url;

public class UrlDAOJDBCTemplateImpl implements UrlDAO {

	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	public void save(Url url) {
		String query = "insert into recrd (recordID, url,adresse,tel,garde) values (?,?,?,?,?)";

		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		Object[] args = new Object[] {url.getRecordID(), url.getUrl(), url.getAdresse(),url.getTel(),url.getGarde()};

		int out = jdbcTemplate.update(query, args);


		if(out !=0){

		}else ;
	}


	public Url getById(int recordID) {
		final String query = "select recordID, url from recrd where recordID = ?";
		final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		//using RowMapper anonymous class, we can create a separate RowMapper for reuse
		//RowMapper anonymous class implementation to map the ResultSet data
		//to Url bean object in queryForObject() method.
		Url url = jdbcTemplate.queryForObject
				(query, new Object[]{recordID}, new RowMapper<Url>(){

					public Url mapRow(ResultSet rs, int rowNum) throws SQLException{
						Url url = new Url();
						url.setRecordID(rs.getInt("recordID"));
						url.setUrl(rs.getString("url"));
						Object[] args = new Object[] {url.getRecordID(), url.getUrl()};

						return url;
					}});



		return url;
	}

	public void update(Url url) {
		String query = "update recrd set garde=? where recordID=?";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		Object[] args = new Object[] {url.getGarde(),url.getRecordID()};

		int out = jdbcTemplate.update(query, args);
		if(out !=0){
			System.out.println("Url updated with id="+url.getRecordID());
		}else System.out.println("No Url found with id="+url.getRecordID());
	}
	// Liste des pharmacies
	public List<Url> getAll() {
		String query = "select recordID, url from recrd";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Url> empList = new ArrayList<Url>();

		List<Map<String,Object>> empRows = jdbcTemplate.queryForList(query);

		for(Map<String,Object> empRow : empRows){
			Url emp = new Url();
			emp.setRecordID(Integer.parseInt(String.valueOf(empRow.get("recordID"))));
			emp.setUrl(String.valueOf(empRow.get("url")));
			empList.add(emp);
		}
		return empList;
	}

	// Vider la table afin de ne pas avoir une redondance des pharmacies dans la BD
	public void runSql2(String sql){
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

		jdbcTemplate.execute(sql);

		System.out.println("Trancate DONE!");

	}
	//verifier si la pharmacie exist deja dans la base de donnees
	public int isUrlExists(String url) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		String sql = "SELECT count(*) FROM recrd WHERE url = ?";
		int res	=0;
		int count = jdbcTemplate.queryForObject(sql, new Object[] { url }, Integer.class);

		if (count > 0) 
			res=count;

		return res;
	}
	// Veifier si la pharmacie est une pharmacie de garde
	public boolean isPharmaGarde(String pharmacie) throws IOException{
		boolean result=false;
		Document doc = Jsoup.connect("http://www.agadirbonsplans.com/pharmacie-de-garde-agadir/").timeout(10*10000).get(); 
		int i = 0;
		Elements newsGarde = doc.select("div .pharmaTitle");
		for (int j= 0; j < newsGarde.size(); j++){
			Element garde = newsGarde.get(j); 
			String pharmaciee = pharmacie.replaceAll(" ", ""); 
			String gardee = garde.text().replaceAll(" ", ""); 

			do{   
				if(!pharmaciee.equals(gardee)){
					result=false;	
					//System.out.println(pharmaciee +"VS"+gardee);
					}
				else {result=true;return result; }
			}while((result==true) || (j >newsGarde.size())); }
		return result;

	}
	// parcourir la page pour recuperer toutes les pharmacies
	public void processPage(String pharma,String adresse,String tel,boolean garde) throws IOException{
		//check if the given URL is already in database
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring.xml");
		UrlDAO urlDAO = ctx.getBean("urlDAO", UrlDAO.class);


		int n=urlDAO.isUrlExists(pharma);

		// la pharma deja exist
		if(n==1){



		}else{
			Url url= new Url();

			url.setUrl(pharma);
			url.setAdresse(adresse);
			url.setTel(tel);
			url.setGarde(garde);
			urlDAO.save(url);

			Document doc = Jsoup.connect("http://www.anahna.com/pharmacies-agadir-ca7-qa0.html").timeout(10*10000).get(); 

			Elements newsHeadlines = doc.select("h1");
			Elements questions = doc.select("div .right").select("p:eq(1)");
			Elements tels = doc.select("div.right").select("p:eq(2)");


			for (int i = 1; i <53; i++) {
				Element elem = newsHeadlines.get(i);
				Element adress = questions.get(i-1);
				Element tele = tels.get(i-1);

				boolean b=false;
				if((elem.text().contains("Pharmacie"))&&(tele.text().contains("05"))){

					processPage(elem.text(),adress.text(),tele.text(),b);


				}}}



	}


}