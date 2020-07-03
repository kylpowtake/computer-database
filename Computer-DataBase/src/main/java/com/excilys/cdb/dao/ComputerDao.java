package com.excilys.cdb.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import com.excilys.cdb.mappers.ComputerMapper;
import com.excilys.cdb.model.Computer;
import com.excilys.cdb.persistence.ConnectionH2;
import com.excilys.cdb.persistence.ConnectionMysql;
import com.excilys.cdb.persistence.Connector;

public class ComputerDao extends AbstractDao<Computer>{
	
	private static String insertSql = "INSERT INTO computer (name) VALUES(?)";
	
	private static String findAllSql = "SELECT computer.id,computer.name,introduced,discontinued,company_id,cp.name as companyName FROM computer LEFT JOIN company as cp on computer.company_id = cp.id";
	
	private static String findSql = "SELECT computer.id,computer.name,introduced,discontinued,company_id,cp.name as companyName FROM computer LEFT JOIN company cp on computer.company_id = cp.id WHERE computer.id = ?;";
	
	private static String deleteSql = "DELETE FROM computer WHERE id = ?";
	
	private static String countSql = "SELECT COUNT(id) FROM computer";
	
	private static String countSearchSql = "SELECT COUNT(computer.id) FROM computer LEFT JOIN company as cp on computer.company_id = cp.id WHERE computer.name like ? OR cp.name like ?";
	
	private String limitOrderSql(String order, String ascending) {
		return "SELECT computer.id,computer.name,introduced,discontinued,company_id,cp.name as companyName "
				+ "FROM computer LEFT JOIN company as cp on computer.company_id = cp.id "
				+ "Order By "+order+" IS NULL, "+order+ " "+ascending+" LIMIT ?,?";
	}
	
	private String limitSearchOrderSql(String order, String ascending) {
		return "SELECT computer.id,computer.name,introduced,discontinued,company_id,cp.name as companyName "
				+ "FROM computer LEFT JOIN company as cp on computer.company_id = cp.id "
				+ "WHERE computer.name like ? OR cp.name like ? Order By "+order+" IS NULL, "+order + " " +ascending+" LIMIT ?,?";
	}
	
	private static Logger logger = LoggerFactory.getLogger(ComputerDao.class);
	
	private Connector connector;
	
	public ComputerDao() {
		this.connector = new ConnectionMysql();
	}
	
	public ComputerDao(int h2) {
		this.connector = new ConnectionH2();
	}
	
	@Override
	public Computer create(Computer obj) {
		Computer comp = new Computer();
		try(Connection connect = connector.getInstance();
			PreparedStatement prepare = connect.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
			prepare.setString(1, obj.getName());
			prepare.executeUpdate();
			ResultSet resultKeys = prepare.getGeneratedKeys();
			if(resultKeys.first()) {
				Integer computerId = resultKeys.getInt(1);
				obj.setIdComputer(computerId);
				this.update(obj);
				comp = this.find(computerId);
			}	
		}catch(SQLException eSQL) {
			logger.error("Error Created Computer");
			logger.error(eSQL.getMessage());
			eSQL.printStackTrace();
		}
		return comp;
	}

	@Override
	public Computer find(Integer id) {
		Computer computer = null;
		try(Connection connect = connector.getInstance();
			PreparedStatement prepare = connect.prepareStatement(findSql)) {
			prepare.setInt(1, id);
			ResultSet result = prepare.executeQuery();
			if(result.next()) {
				computer = ComputerMapper.resultToObject(result);
			}
		}catch(SQLException eSQL) {
			logger.error("Error Getting computer");
			eSQL.printStackTrace();
		}
		return computer;
	}

	@Override
	public List<Computer> findAll() {
		List<Computer> allComputer = new ArrayList<>();
		try(Connection connect = connector.getInstance();
			ResultSet result = connect.createStatement().executeQuery(findAllSql)) {
			while(result.next()) {
				Computer computer = ComputerMapper.resultToObject(result);
				allComputer.add(computer);
			}
		}catch(SQLException eSQL) {
			logger.error("Error Getting computers");
			eSQL.printStackTrace();
		}
		return allComputer;
	}

	@Override
	public Computer update(Computer obj) {
		try(Connection connect = connector.getInstance()) {
			String sqlRequest = "UPDATE computer SET name = '" + obj.getName() + "'";
			if(obj.getIntroduced() != null) {
				sqlRequest += ", introduced = '" + Date.valueOf(obj.getIntroduced()) + "'";
			}			
			if(obj.getDiscontinued() != null) {
				sqlRequest += ", discontinued = '" + Date.valueOf(obj.getDiscontinued()) + "'";
			}		
			if(obj.getCompany() != null && obj.getCompany().getIdCompany() != null) {
				sqlRequest += ", company_id = " +obj.getCompany().getIdCompany() + ""; 
			}		
			sqlRequest += " WHERE id = "+ obj.getIdComputer();
			connect.createStatement().executeUpdate(sqlRequest);
			return this.find(obj.getIdComputer());
		}catch(SQLException eSQL) {
			logger.error("Error Update Computer");
			eSQL.printStackTrace();
		}
		return null;
	}

	@Override
	public void delete(Integer id) {
		try(Connection connect = connector.getInstance();
			PreparedStatement prepare = connect.prepareStatement(deleteSql)){
			prepare.setInt(1, id);
			prepare.executeUpdate();
		}catch(SQLException eSQL) {
			logger.error("Error Delete Computer");
			eSQL.printStackTrace();
		}
	}


	@Override
	public List<Computer> findBetween(Integer offset, Integer nb, String order, String ascending) {
		List<Computer> allComputer = new ArrayList<>();
		try(Connection connect = connector.getInstance();
			PreparedStatement prepare = connect.prepareStatement(limitOrderSql(order, ascending))) {
			prepare.setInt(1, offset);
			prepare.setInt(2, nb);
			ResultSet result = prepare.executeQuery();
			while(result.next()) {
				Computer computer = ComputerMapper.resultToObject(result);
				allComputer.add(computer);
			}
		}catch(SQLException eSQL) {
			logger.error("Error Getting Computers between");
			eSQL.printStackTrace();
		}
		return allComputer;
	}

	
	@Override
	public Integer count() {
		Integer nb = 0;
		try(Connection connect = connector.getInstance();
			ResultSet result =connect.createStatement().executeQuery(countSql)) {
			if(result.next()) {
				nb =result.getInt(1);
			}
		}catch(SQLException eSQL) {
			logger.error("Error counting Computers");
			eSQL.printStackTrace();
		}
		return nb;
	}
	
	public List<Computer> findBetweenWithSearch(Integer offset, Integer nb, String search, String order, String ascending){
		List<Computer> allComputer = new ArrayList<>();
		try(Connection connect = connector.getInstance();
			PreparedStatement prepare = connect.prepareStatement(limitSearchOrderSql(order, ascending))) {
			prepare.setString(1, "%"+search+"%");
			prepare.setString(2, "%"+search+"%");
			prepare.setInt(3, offset);
			prepare.setInt(4, nb);
			ResultSet result = prepare.executeQuery();
			while(result.next()) {
				Computer computer = ComputerMapper.resultToObject(result);
				allComputer.add(computer);
			}
		}catch(SQLException eSQL) {
			logger.error("Error Getting Computers between Search");
			eSQL.printStackTrace();
		}
		return allComputer;
	}
	
	public Integer countSearch(String search) {
		Integer nb = 0;
		try(Connection connect = connector.getInstance();
			PreparedStatement prepare = connect.prepareStatement(countSearchSql)) {
			prepare.setString(1, "%"+search+"%");
			prepare.setString(2, "%"+search+"%");
			ResultSet result = prepare.executeQuery();
			if(result.next()) {
				nb =result.getInt(1);
			}
		}catch(SQLException eSQL) {
			logger.error("Error counting Computers Search");
			eSQL.printStackTrace();
		}
		return nb;
	}

}
