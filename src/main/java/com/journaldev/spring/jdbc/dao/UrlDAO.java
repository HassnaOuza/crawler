package com.journaldev.spring.jdbc.dao;

import java.io.IOException;
import java.util.List;

import com.journaldev.spring.jdbc.model.Url;
 
//CRUD operations
public interface UrlDAO {
     
    //Create
    public void save(Url url);
    //Read
    public Url getById(int recordID);
    //Update
    public void update(Url url);
 
    //Get All
    public List<Url> getAll();
    public void runSql2(String sql);
    public void processPage(String pharma,String adresse,String tel,boolean garde) throws IOException;
    public int isUrlExists(String recordID);
    public boolean isPharmaGarde(String pharmacie) throws IOException;
}