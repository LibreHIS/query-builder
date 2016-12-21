/*
 * Created on 02-Dec-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.ims.query.builder;

import java.util.ArrayList;

public class Clause
{
	public static final int CLAUSE_ALL = 1;
	public static final int CLAUSE_ANY = 2;
	public static final int CLAUSE_NONE = 3;
	public static final int CLAUSE_NOT_ALL = 4;
	public static final int NO_CLAUSE = 0;
	
	private int id;
	private Clause parent;
	private int clause;
	private String hql;
	private String validator;
	private ArrayList children;
		
	public Clause()
	{
		this.id = 0;
		this.parent = null;
		this.clause = NO_CLAUSE;
		this.children = new ArrayList();
	}
	
	public Clause(int id, Clause parent, int clause, String hql, String validator)
	{
		this.id = id;
		this.parent = parent;
		this.clause = clause;
		this.hql = hql;
		this.validator = validator;
		
		this.children = new ArrayList();
	}

	public ArrayList getChildren()
	{
		return children;
	}
	public Clause getChildrenAt(int index)
	{
		return (Clause)children.get(index);
	}
	public void setChildrenAt(int index, Clause clause)
	{
		children.set(index, clause);
	}
	public void setChildren(ArrayList children)
	{
		this.children = children;
	}
	public void addChildren(Clause clause)
	{
		clause.setParent(this);
		children.add(clause);
	}

	public int getClause()
	{
		return clause;
	}
	public void setClause(int clause)
	{
		this.clause = clause;
	}
	public String getHql()
	{
		return hql;
	}
	public void setHql(String hql)
	{
		this.hql = hql;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public Clause getParent()
	{
		return parent;
	}
	public void setParent(Clause parent)
	{
		this.parent = parent;
	}
	public String getValidator()
	{
		return validator;
	}
	public void setValidator(String validator)
	{
		this.validator = validator;
	}
	
	
}
