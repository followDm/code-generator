package com.common.generator.ext;

import java.util.Scanner;

import com.common.generator.util.StringHelper;
import com.common.generator.util.SystemHelper;
import com.common.generator.GeneratorFacade;
import com.common.generator.GeneratorProperties;
import com.common.generator.util.ArrayHelper;

/**
 * 命令行工具类,可以直接运行
 * 
 * @modify diyvan
 */
public class CommandLine {
	
	public static void main(String[] args) throws Exception {
		//disable freemarker logging
		freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);
		
		startProcess();
	}

	private static void startProcess() throws Exception {
		Scanner sc = new Scanner(System.in);
		//System.out.println("templateRootDir:"+new File(getTemplateRootDir()).getAbsolutePath());
		//输出指令信息
		printUsages();
		while(sc.hasNextLine()) {
			try {
				//启动命令行
				processLine(sc);
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				Thread.sleep(700);
				printUsages();
			}
		}
		
	}

	private static void processLine(Scanner sc) throws Exception {
		GeneratorFacade facade = new GeneratorFacade();
		String cmd = sc.next();
		
		if("geni".equals(cmd)) {
			//根据数据库表生成ibaitis代码
			String[] args = nextArguments(sc);
			if(args.length == 0) return;
			facade.g.setIncludes(getIncludes(args,1));
			facade.generateByTable(args[0],getTemplateRootDir("template_umsfi"));
			if(SystemHelper.isWindowsOS) {
			    Runtime.getRuntime().exec("cmd.exe /c start "+GeneratorProperties.getRequiredProperty("outRoot").replace('/', '\\'));
			}
		}else if("genh".equals(cmd)){
			//根据数据库表生成hibernate代码
			String[] args = nextArguments(sc);
			if(args.length == 0) return;
			facade.g.setIncludes(getIncludes(args,1));
			facade.generateByTable(args[0],getTemplateRootDir("template_sfh"));
			if(SystemHelper.isWindowsOS) {
			    Runtime.getRuntime().exec("cmd.exe /c start "+GeneratorProperties.getRequiredProperty("outRoot").replace('/', '\\'));
			}
		}else if("genpi".equals(cmd)){
			//根据存储过程(包名.过程名)生成ibatis代码
			
		}else if("deli".equals(cmd)) {
			//根据数据库中的表删除生成的ibatis代码
			String[] args = nextArguments(sc);
			if(args.length == 0) return;
			facade.g.setIncludes(getIncludes(args,1));
			facade.deleteByTable(args[0], getTemplateRootDir("template_umsfi"));
		}else if("delh".equals(cmd)) {
			//根据数据库中的表删除生成的ibatis代码
			String[] args = nextArguments(sc);
			if(args.length == 0) return;
			facade.g.setIncludes(getIncludes(args,1));
			facade.deleteByTable(args[0], getTemplateRootDir("template_sfh"));
		}else if("quit".equals(cmd)) {
			//退出
			System.exit(0);
		}else {
			System.err.println(" [ERROR] unknow command:"+cmd);
		}
	}

	private static String getIncludes(String[] args, int i) {
		String includes = ArrayHelper.getValue(args, i);
		if(includes == null) return null;
		return includes.indexOf("*") >= 0 || includes.indexOf(",") >= 0 ? includes : includes+"/**";
	}
	
	
	/**
	 * 获取模板类型
	 * @param templateType
	 * @return
	 */
	private static String getTemplateRootDir(String templateType) {
		return System.getProperty("templateRootDir", templateType);
	}

	/**
	 * geni  根据数据库中的表名生成ibatis代码
	 * genh  根据数据库中的表名生成hibernate代码
	 * genpi 根据存储过程(包名.过程名)生成ibatis代码
	 * deli  根据数据库中的表删除生成的ibatis代码
	 * delh  根据数据库中的表删除生成的hibernate代码
	 */
	private static void printUsages() {
		System.out.println("Usage:");
		System.out.println("\tgeni  table_name : 根据数据库中的表名生成ibatis代码");
		System.out.println("\tgenh  table_name : 根据数据库中的表名生成hibernate代码");
//		System.out.println("\tgenpi package_name.procedure_name : 根据存储过程(包名.过程名)生成ibatis代码");
		System.out.println("\tdeli  table_name : 根据数据库中的表删除生成的ibatis代码");
		System.out.println("\tdelh  table_name : 根据数据库中的表删除生成的hibernate代码");
		System.out.println("\tquit : quit");
		System.out.println("\t[include_path] subdir of templateRootDir,example: 1. dao  2. dao/**,service/**");
		System.out.print("please input command:");
		
	}
	
	/**
	 * 输出用户在generator.xml中填写的相关信息
	 */
	private static void printBaseInfo(){
		String targetObject = GeneratorProperties.getRequiredProperty("targetObject");//目标对象
		String templateType = GeneratorProperties.getRequiredProperty("templateType");//模板类型
		String basepackage = GeneratorProperties.getRequiredProperty("basepackage");//基本包路径
		String namespace = GeneratorProperties.getRequiredProperty("namespace");//视图路径
		String outRoot = GeneratorProperties.getRequiredProperty("outRoot");//输出路径
		System.out.println("代码生成器配置信息:");
		System.out.println("\t 目标对象："+targetObject);
		System.out.println("\t 模板类型："+templateType);
		System.out.println("\t 基本包路径："+basepackage);
		System.out.println("\t 视图包：："+namespace);
		System.out.println("\t 输出路径：："+outRoot);
	}
	
	private static String[] nextArguments(Scanner sc) {
		return StringHelper.tokenizeToStringArray(sc.nextLine()," ");
	}
}
