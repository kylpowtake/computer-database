package com.excilys.cdb.dao.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.excilys.cdb.model.Company;

public class CompanyDaoMapper {
	
	private CompanyDaoMapper() {
	}
	
	public static Company resultToObject(ResultSet result) throws SQLException {
		Company company = new Company(result.getInt("id"), result.getString("name"));
		return company;
	}

}