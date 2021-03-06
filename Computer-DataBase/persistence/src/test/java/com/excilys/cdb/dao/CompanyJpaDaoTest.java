package com.excilys.cdb.dao;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.excilys.cdb.model.Company;
import com.excilys.cdb.model.Computer;
import com.excilys.cdb.model.Page;

public class CompanyJpaDaoTest {
	
	private AbstractJpaDao<Company> companyDao;
	
	private AbstractJpaDao<Computer> computerDao;
	
	private Page page = Mockito.mock(Page.class);

	@Before
	public void setUp() throws Exception {
		Mockito.when(page.getItemsByPage()).thenReturn(10);
		Mockito.when(page.getAscending()).thenReturn("ASC");
		Mockito.when(page.getOrder()).thenReturn("company.id");
	}

	@Test
	public void findAll() {
		assertTrue(companyDao.findAll().size() == 20);
	}
	
	@Test
	public void find() {
		Company company = companyDao.find(1);
		Company companyExpected = new Company(1, "Apple Inc.");
		assertEquals(companyExpected, company);
	}
	
	@Test
	public void count() {
		assertTrue(companyDao.count() == 20);
	}
	
	@Test
	public void findBetween(){
		assertTrue(companyDao.findBetween(0, page).size() == 10);
	}
	
	@Test
	public void delete() {
		companyDao.delete(1);
		assertTrue(companyDao.count() == 19);
		assertTrue(computerDao.count()== 32);
		
	}

}
