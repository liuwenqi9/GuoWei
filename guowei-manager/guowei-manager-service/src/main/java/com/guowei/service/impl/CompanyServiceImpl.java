package com.guowei.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.guowei.common.pojo.DatatablesView;
import com.guowei.mapper.GwCompanyMapper;
import com.guowei.mapper.GwCompanyprochangeMapper;
import com.guowei.mapper.GwCompanyproductMapper;
import com.guowei.mapper.GwDivisionMapper;
import com.guowei.mapper.GwProductMapper;
import com.guowei.mapper.GwTemplateproductMapper;
import com.guowei.pojo.GwCompany;
import com.guowei.pojo.GwCompanyExample;
import com.guowei.pojo.GwCompanyExample.Criteria;
import com.guowei.pojo.GwCompanyTemp;
import com.guowei.pojo.GwCompanyprochangeExample;
import com.guowei.pojo.GwCompanyproduct;
import com.guowei.pojo.GwCompanyproductExample;
import com.guowei.pojo.GwDivision;
import com.guowei.pojo.GwDivisionExample;
import com.guowei.pojo.GwProduct;
import com.guowei.pojo.GwTemplateproduct;
import com.guowei.pojo.GwTemplateproductExample;
import com.guowei.service.CompanyService;

/**
 * 公司管理Service
 * 
 * @author 陈安一
 *
 */
@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {
	@Autowired
	private GwCompanyMapper companyMapper;
	@Autowired
	private GwDivisionMapper divisionMapper;
	@Autowired
	private GwProductMapper productMapper;
	@Autowired
	private GwCompanyproductMapper companyproductMapper;
	@Autowired
	private GwCompanyprochangeMapper companyprochangeMapper;
	@Autowired
	private GwTemplateproductMapper templateproductMapper;

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public GwCompany getGwCompanyById(long parseLong) {
		GwCompany res = companyMapper.selectByPrimaryKey(parseLong);
		return res;
	}

	@Override
	public int addGwCompany(GwCompany company) {
		// 1、获取模板商品
		GwTemplateproductExample example1 = new GwTemplateproductExample();
		example1.createCriteria().andTidEqualTo(company.getTemplateId());
		List<GwTemplateproduct> list2 = new ArrayList<GwTemplateproduct>();
		if (company.getSectemplateId() > 0) {
			GwTemplateproductExample example2 = new GwTemplateproductExample();
			example2.createCriteria().andTidEqualTo(company.getSectemplateId());
			list2 = templateproductMapper.selectByExample(example2);
		}

		List<GwTemplateproduct> list1 = templateproductMapper.selectByExample(example1);
		int res = companyMapper.insert(company);
		int addCpResult = 1;
		int addSecCpResult = 1;
		for (GwTemplateproduct gwTemplateproduct : list1) {
			// 2、依次添加为公司产品
			GwCompanyproduct cp = new GwCompanyproduct();
			cp.setCompanyId(company.getId());
			cp.setProname(gwTemplateproduct.getProname());
			cp.setProimage(gwTemplateproduct.getProimage());
			cp.setProprice(gwTemplateproduct.getProprice());
			cp.setPid(gwTemplateproduct.getPid());
			cp.setSellcount(0);
			cp.setSellprice(gwTemplateproduct.getProprice());
			cp.setStatus(Byte.parseByte("1"));
			cp.setStock(gwTemplateproduct.getStock());

			GwProduct pro = productMapper.selectByPrimaryKey(gwTemplateproduct.getPid());
			int updatePro = 0;
			if (pro != null) {
				pro.setDistribute(pro.getDistribute() + gwTemplateproduct.getStock());
				pro.setStock(pro.getStock() - gwTemplateproduct.getStock());
				updatePro = productMapper.updateByPrimaryKey(pro);
			}

			cp.setWarnstock(gwTemplateproduct.getWarnstock());
			cp.setStorageracks(gwTemplateproduct.getStorageracks());
			int insertResult = companyproductMapper.insert(cp);
			if (insertResult != 1 || updatePro != 1) {
				addCpResult = 0;
			}
		}

		for (GwTemplateproduct gwTemplateproduct : list2) {
			// 2、依次添加为公司产品
			GwCompanyproduct cp = new GwCompanyproduct();
			cp.setCompanyId(company.getId());
			cp.setProname(gwTemplateproduct.getProname());
			cp.setProimage(gwTemplateproduct.getProimage());
			cp.setProprice(gwTemplateproduct.getProprice());
			cp.setPid(gwTemplateproduct.getPid());
			cp.setSellcount(0);
			cp.setSellprice(gwTemplateproduct.getProprice());
			cp.setStatus(Byte.parseByte("1"));
			cp.setStock(gwTemplateproduct.getStock());

			GwProduct pro = productMapper.selectByPrimaryKey(gwTemplateproduct.getPid());
			int updatePro = 0;
			if (pro != null) {
				pro.setDistribute(pro.getDistribute() + gwTemplateproduct.getStock());
				pro.setStock(pro.getStock() - gwTemplateproduct.getStock());
				updatePro = productMapper.updateByPrimaryKey(pro);
			}

			cp.setWarnstock(gwTemplateproduct.getWarnstock());
			cp.setStorageracks(gwTemplateproduct.getStorageracks());
			int insertResult = companyproductMapper.insert(cp);
			if (insertResult != 1 || updatePro != 1) {
				addSecCpResult = 0;
			}
		}
		return (res == 1 && addCpResult == 1 && addSecCpResult == 1) ? 1 : 0;
	}

	@Override
	public int editGwCompany(GwCompany company) {
		GwCompany temp = companyMapper.selectByPrimaryKey(company.getId());
		int updateCpResult = 1;
		int updateSecCpResult = 1;
		int deleteResult = 1;
		int returnStock = 1;
		if ((company.getTemplateId() != null && temp.getTemplateId() != company.getTemplateId())
				|| (company.getSectemplateId() != null && temp.getSectemplateId() != company.getSectemplateId())) {
			// 替换公司模板
			// 1、删除原公司产品
			GwCompanyproductExample example1 = new GwCompanyproductExample();
			example1.createCriteria().andCompanyIdEqualTo(company.getId());

			// 将原公司产品库存返还至商品总库存
			List<GwCompanyproduct> oldcps = companyproductMapper.selectByExample(example1);
			if (oldcps != null && oldcps.size() > 0) {
				for (GwCompanyproduct gwCompanyproduct : oldcps) {
					Long pid = gwCompanyproduct.getPid();
					GwProduct temppro = productMapper.selectByPrimaryKey(pid);
					// 返还库存
					temppro.setStock(temppro.getStock() + gwCompanyproduct.getStock());
					// 返还已铺货数量
					temppro.setDistribute(temppro.getDistribute() - gwCompanyproduct.getStock());
					int tempreturn = productMapper.updateByPrimaryKey(temppro);
					if (tempreturn != 1) {
						returnStock = 0;
					}
					GwCompanyprochangeExample ex = new GwCompanyprochangeExample();
					ex.createCriteria().andCpidEqualTo(gwCompanyproduct.getId());
					companyprochangeMapper.deleteByExample(ex);
				}
			}

			deleteResult = companyproductMapper.deleteByExample(example1);
			// 2、获取模板商品
			GwTemplateproductExample example2 = new GwTemplateproductExample();
			example2.createCriteria().andTidEqualTo(company.getTemplateId());

			List<GwTemplateproduct> list1 = templateproductMapper.selectByExample(example2);

			List<GwTemplateproduct> list2 = new ArrayList<GwTemplateproduct>();
			if (company.getSectemplateId() > 0) {
				GwTemplateproductExample example3 = new GwTemplateproductExample();
				example3.createCriteria().andTidEqualTo(company.getSectemplateId());
				list2 = templateproductMapper.selectByExample(example3);
			}

			for (GwTemplateproduct gwTemplateproduct : list1) {
				// 3、依次添加为公司产品
				GwCompanyproduct cp = new GwCompanyproduct();
				cp.setCompanyId(company.getId());
				cp.setPid(gwTemplateproduct.getPid());
				cp.setProname(gwTemplateproduct.getProname());
				cp.setProimage(gwTemplateproduct.getProimage());
				cp.setProprice(gwTemplateproduct.getProprice());
				cp.setSellcount(0);
				cp.setSellprice(gwTemplateproduct.getProprice());
				cp.setStatus(Byte.parseByte("1"));
				cp.setStock(gwTemplateproduct.getStock());
				GwProduct pro = productMapper.selectByPrimaryKey(gwTemplateproduct.getPid());
				int updatePro = 0;
				if (pro != null) {
					pro.setDistribute(pro.getDistribute() + gwTemplateproduct.getStock());
					pro.setStock(pro.getStock() - gwTemplateproduct.getStock());
					updatePro = productMapper.updateByPrimaryKey(pro);
				}

				cp.setWarnstock(gwTemplateproduct.getWarnstock());
				cp.setStorageracks(gwTemplateproduct.getStorageracks());
				int insertResult = companyproductMapper.insert(cp);
				if (insertResult != 1 || updatePro != 1) {
					updateCpResult = 0;
				}
			}

			for (GwTemplateproduct gwTemplateproduct : list2) {
				// 3、依次添加为公司产品
				GwCompanyproduct cp = new GwCompanyproduct();
				cp.setCompanyId(company.getId());
				cp.setPid(gwTemplateproduct.getPid());
				cp.setProname(gwTemplateproduct.getProname());
				cp.setProimage(gwTemplateproduct.getProimage());
				cp.setProprice(gwTemplateproduct.getProprice());
				cp.setSellcount(0);
				cp.setSellprice(gwTemplateproduct.getProprice());
				cp.setStatus(Byte.parseByte("1"));
				cp.setStock(gwTemplateproduct.getStock());
				GwProduct pro = productMapper.selectByPrimaryKey(gwTemplateproduct.getPid());
				int updatePro = 0;
				if (pro != null) {
					pro.setDistribute(pro.getDistribute() + gwTemplateproduct.getStock());
					pro.setStock(pro.getStock() - gwTemplateproduct.getStock());
					updatePro = productMapper.updateByPrimaryKey(pro);
				}

				cp.setWarnstock(gwTemplateproduct.getWarnstock());
				cp.setStorageracks(gwTemplateproduct.getStorageracks());
				int insertResult = companyproductMapper.insert(cp);
				if (insertResult != 1 || updatePro != 1) {
					updateSecCpResult = 0;
				}
			}
		}

		int res = companyMapper.updateByPrimaryKey(company);
		return (res == 1 && updateCpResult == 1 && deleteResult == 1 && returnStock == 1 && updateSecCpResult == 1) ? 1
				: 0;
	}

	@Override
	public int removeGwCompany(long id) {
		GwCompanyproductExample example = new GwCompanyproductExample();
		example.createCriteria().andCompanyIdEqualTo(id);
		int returnStock = 1;
		// 将原公司产品库存返还至商品总库存
		List<GwCompanyproduct> oldcps = companyproductMapper.selectByExample(example);
		if (oldcps != null && oldcps.size() > 0) {
			for (GwCompanyproduct gwCompanyproduct : oldcps) {
				Long pid = gwCompanyproduct.getPid();
				GwProduct temppro = productMapper.selectByPrimaryKey(pid);
				// 返还库存
				temppro.setStock(temppro.getStock() + gwCompanyproduct.getStock());
				// 返还已铺货数量
				temppro.setDistribute(temppro.getDistribute() - gwCompanyproduct.getStock());
				int tempreturn = productMapper.updateByPrimaryKey(temppro);
				if (tempreturn != 1) {
					returnStock = 0;
				}
			}
		}

		int deleteResult = companyproductMapper.deleteByExample(example);
		int res = companyMapper.deleteByPrimaryKey(id);
		return (res == 1 && deleteResult == 1 && returnStock == 1) ? 1 : 0;
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DatatablesView<?> getGwCompanysByPagedParam(GwCompany company, Integer start, Integer pageSize) {
		GwCompanyExample gme = new GwCompanyExample();
		Criteria criteria = gme.createCriteria();
		if (!"".equals(company.getCompanyName())) {
			criteria.andCompanyNameLike("%" + company.getCompanyName() + "%");
			gme.or(gme.createCriteria().andCompanyContactphoneLike("%" + company.getCompanyName() + "%"));
		}
		// 根据管理员区划查询公司
		if (company.getDid() != null && company.getDid() > 0) {
			GwDivision temp = divisionMapper.selectByPrimaryKey(company.getDid());
			if (temp != null) {
				if (temp.getLevel() == 1) {
					// 包含两级区划
					List<Long> areas = new ArrayList<Long>();
					// 查询二级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andPidEqualTo(company.getDid());
					List<GwDivision> divisions = divisionMapper.selectByExample(g);

					for (GwDivision gwDivision : divisions) {
						// 加入二级区划
						areas.add(gwDivision.getId());
						GwDivisionExample g2 = new GwDivisionExample();
						com.guowei.pojo.GwDivisionExample.Criteria c2 = g2.createCriteria();
						c2.andPidEqualTo(gwDivision.getId());
						// 查询三级区划
						List<GwDivision> divisions2 = divisionMapper.selectByExample(g2);
						for (GwDivision division2 : divisions2) {
							// 加入三级区划
							areas.add(division2.getId());
						}
					}
					areas.add(company.getDid());
					criteria.andDidIn(areas);
				} else if (temp.getLevel() == 2) {
					// 有一级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andIdEqualTo(company.getDid());
					g.or(g.createCriteria().andPidEqualTo(company.getDid()));
					List<GwDivision> divisions = divisionMapper.selectByExample(g);
					List<Long> areas = new ArrayList<Long>();
					for (GwDivision gwDivision : divisions) {
						areas.add(gwDivision.getId());
					}
					criteria.andDidIn(areas);
				} else {
					// 无下级区划
					criteria.andDidEqualTo(company.getDid());
				}
			}
		}
		int pageNum = (start / pageSize) + 1;
		PageHelper.startPage(pageNum, pageSize);
		List<GwCompany> list = companyMapper.selectByExample(gme);
		PageInfo<GwCompany> page = new PageInfo<>(list);
		DatatablesView result = new DatatablesView();
		result.setData(list);
		result.setRecordsTotal((int) page.getTotal());
		return result;
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public DatatablesView<?> getGwCompanysByParam(GwCompany company) {
		GwCompanyExample gme = new GwCompanyExample();
		Criteria criteria = gme.createCriteria();
		if (!"".equals(company.getCompanyName())) {
			criteria.andCompanyNameLike("%" + company.getCompanyName() + "%");
			gme.or(gme.createCriteria().andCompanyContactphoneLike("%" + company.getCompanyName() + "%"));
		}
		if (company.getTemplateId() > 0) {
			criteria.andTemplateIdEqualTo(company.getTemplateId());
		}
		// 根据管理员区划查询公司
		if (company.getDid() != null && company.getDid() > 0) {
			GwDivision temp = divisionMapper.selectByPrimaryKey(company.getDid());
			if (temp != null) {
				if (temp.getLevel() == 1) {
					// 包含两级区划
					List<Long> areas = new ArrayList<Long>();
					// 查询二级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andPidEqualTo(company.getDid());
					List<GwDivision> divisions = divisionMapper.selectByExample(g);

					for (GwDivision gwDivision : divisions) {
						// 加入二级区划
						areas.add(gwDivision.getId());
						GwDivisionExample g2 = new GwDivisionExample();
						com.guowei.pojo.GwDivisionExample.Criteria c2 = g2.createCriteria();
						c2.andPidEqualTo(gwDivision.getId());
						// 查询三级区划
						List<GwDivision> divisions2 = divisionMapper.selectByExample(g2);
						for (GwDivision division2 : divisions2) {
							// 加入三级区划
							areas.add(division2.getId());
						}
					}
					areas.add(company.getDid());
					criteria.andDidIn(areas);
				} else if (temp.getLevel() == 2) {
					// 有一级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andIdEqualTo(company.getDid());
					g.or(g.createCriteria().andPidEqualTo(company.getDid()));
					List<GwDivision> divisions = divisionMapper.selectByExample(g);
					List<Long> areas = new ArrayList<Long>();
					for (GwDivision gwDivision : divisions) {
						areas.add(gwDivision.getId());
					}
					criteria.andDidIn(areas);
				} else {
					// 无下级区划
					criteria.andDidEqualTo(company.getDid());
				}
			}
		}

		List<GwCompany> list = companyMapper.selectByExample(gme);
		DatatablesView result = new DatatablesView();
		result.setData(list);
		result.setRecordsTotal(list.size());
		return result;
	}

	@Override
	public GwCompanyTemp selectById(Long id) {
		GwCompanyTemp res = companyMapper.selectById(id);
		return res;
	}

	@Override
	public DatatablesView<?> getGwCompanyTempsByParam(GwCompanyTemp company) {
		GwCompanyExample gme = new GwCompanyExample();
		Criteria criteria = gme.createCriteria();
		if (!"".equals(company.getCompanyName())) {
			criteria.andCompanyNameLike("%" + company.getCompanyName() + "%");
			gme.or(gme.createCriteria().andCompanyContactphoneLike("%" + company.getCompanyName() + "%"));
		}

		// 根据管理员区划查询公司
		if (company.getDid() != null && company.getDid() > 0) {
			GwDivision temp = divisionMapper.selectByPrimaryKey(company.getDid());
			if (temp != null) {
				if (temp.getLevel() == 1) {
					// 包含两级区划
					List<Long> areas = new ArrayList<Long>();
					// 查询二级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andPidEqualTo(company.getDid());
					List<GwDivision> divisions = divisionMapper.selectByExample(g);

					for (GwDivision gwDivision : divisions) {
						// 加入二级区划
						areas.add(gwDivision.getId());
						GwDivisionExample g2 = new GwDivisionExample();
						com.guowei.pojo.GwDivisionExample.Criteria c2 = g2.createCriteria();
						c2.andPidEqualTo(gwDivision.getId());
						// 查询三级区划
						List<GwDivision> divisions2 = divisionMapper.selectByExample(g2);
						for (GwDivision division2 : divisions2) {
							// 加入三级区划
							areas.add(division2.getId());
						}
					}
					areas.add(company.getDid());
					criteria.andDidIn(areas);
				} else if (temp.getLevel() == 2) {
					// 有一级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andIdEqualTo(company.getDid());
					g.or(g.createCriteria().andPidEqualTo(company.getDid()));
					List<GwDivision> divisions = divisionMapper.selectByExample(g);
					List<Long> areas = new ArrayList<Long>();
					for (GwDivision gwDivision : divisions) {
						areas.add(gwDivision.getId());
					}
					criteria.andDidIn(areas);
				} else {
					// 无下级区划
					criteria.andDidEqualTo(company.getDid());
				}
			}
		}

		List<GwCompanyTemp> list = companyMapper.selectTempByExample(gme);
		DatatablesView result = new DatatablesView();
		result.setData(list);
		result.setRecordsTotal(list.size());
		return result;
	}

	@Override
	public DatatablesView<?> getGwCompanyTempsByPagedParam(GwCompanyTemp company, Integer start, Integer pageSize) {
		GwCompanyExample gme = new GwCompanyExample();
		Criteria criteria = gme.createCriteria();
		if (!"".equals(company.getCompanyName())) {
			criteria.andCompanyNameLike("%" + company.getCompanyName() + "%");
			gme.or(gme.createCriteria().andCompanyContactphoneLike("%" + company.getCompanyName() + "%"));
		}
		// 根据管理员区划查询公司
		if (company.getDid() != null && company.getDid() > 0) {
			GwDivision temp = divisionMapper.selectByPrimaryKey(company.getDid());
			if (temp != null) {
				if (temp.getLevel() == 1) {
					// 包含两级区划
					List<Long> areas = new ArrayList<Long>();
					// 查询二级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andPidEqualTo(company.getDid());
					List<GwDivision> divisions = divisionMapper.selectByExample(g);

					for (GwDivision gwDivision : divisions) {
						// 加入二级区划
						areas.add(gwDivision.getId());
						GwDivisionExample g2 = new GwDivisionExample();
						com.guowei.pojo.GwDivisionExample.Criteria c2 = g2.createCriteria();
						c2.andPidEqualTo(gwDivision.getId());
						// 查询三级区划
						List<GwDivision> divisions2 = divisionMapper.selectByExample(g2);
						for (GwDivision division2 : divisions2) {
							// 加入三级区划
							areas.add(division2.getId());
						}
					}
					areas.add(company.getDid());
					criteria.andDidIn(areas);
				} else if (temp.getLevel() == 2) {
					// 有一级区划
					GwDivisionExample g = new GwDivisionExample();
					com.guowei.pojo.GwDivisionExample.Criteria c = g.createCriteria();
					c.andIdEqualTo(company.getDid());
					g.or(g.createCriteria().andPidEqualTo(company.getDid()));
					List<GwDivision> divisions = divisionMapper.selectByExample(g);
					List<Long> areas = new ArrayList<Long>();
					for (GwDivision gwDivision : divisions) {
						areas.add(gwDivision.getId());
					}
					criteria.andDidIn(areas);
				} else {
					// 无下级区划
					criteria.andDidEqualTo(company.getDid());
				}
			}
		}		
		int pageNum = (start / pageSize) + 1;
		PageHelper.startPage(pageNum, pageSize);
		List<GwCompanyTemp> list = companyMapper.selectTempByExample(gme);
		PageInfo<GwCompanyTemp> page = new PageInfo<>(list);
		DatatablesView result = new DatatablesView();
		result.setData(list);
		result.setRecordsTotal((int) page.getTotal());
		return result;
	}
}
