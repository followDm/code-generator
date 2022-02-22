package com.common.generator.util.enums;

public enum ProcedureParameterKind {
	NORMAL("NORMAL","普通"),ARRAY("ARRAY","数组"),CURSOR("CURSOR","游标");
	
	private String id;
	private String name;
	
	private ProcedureParameterKind(String id,String name){
		this.id = id;
		this.name = name;
	}
	
	public String getId(){
		return this.id;
	}
	
	public String getName(){
		return this.name;
	}
}
