package com.common.generator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.common.generator.provider.db.sql.model.Sql;
import com.common.generator.provider.db.table.TableFactory;
import com.common.generator.provider.db.table.model.Table;
import com.common.generator.provider.java.model.JavaClass;
import com.common.generator.Generator.GeneratorModel;
import com.common.generator.util.BeanHelper;
import com.common.generator.util.GLogger;
import com.common.generator.util.GeneratorException;
/**
 * 
 * @modify diyvan
 *
 */
public class GeneratorFacade {
	public Generator g = new Generator();
	public GeneratorFacade(){
		//在构造器中设置输出文件目录
		g.setOutRootDir(GeneratorProperties.getProperty("outRoot"));
	}
	
	public static void printAllTableNames() throws Exception {
		PrintUtils.printAllTableNames(TableFactory.getInstance().getAllTables());
	}
	
	/**
	 * 删除生成器的目录
	 * @throws IOException
	 */
	public void deleteOutRootDir() throws IOException {
		g.deleteOutRootDir();
	}
	
	public void generateByMap(Map map,String templateRootDir) throws Exception {
		new ProcessUtils().processByMap(map, templateRootDir,false);
	}

	public void deleteByMap(Map map,String templateRootDir) throws Exception {
		new ProcessUtils().processByMap(map, templateRootDir,true);
	}
	
	public void generateByAllTable(String templateRootDir) throws Exception {
		new ProcessUtils().processByAllTable(templateRootDir,false);
	}
	
	public void deleteByAllTable(String templateRootDir) throws Exception {
		new ProcessUtils().processByAllTable(templateRootDir,true);		
	}
	
    /**
     * 根据数据表名和模板文件路径生成所需要文件(从页面到dao)
     * @param tableName
     * @param templateRootDir
     * @throws Exception
     */
    public void generateByTable(String tableName,String templateRootDir) throws Exception {
    	new ProcessUtils().processByTable(tableName,templateRootDir,false);
	}

    public void deleteByTable(String tableName,String templateRootDir) throws Exception {
    	new ProcessUtils().processByTable(tableName,templateRootDir,true);
	}
    
	/**
	 * 根据实体类生成模板下的文件
	 * @param clazz
	 * @param templateRootDir
	 * @throws Exception
	 */
	public void generateByClass(Class clazz,String templateRootDir) throws Exception {
		new ProcessUtils().processByClass(clazz, templateRootDir,false);
	}

	public void deleteByClass(Class clazz,String templateRootDir) throws Exception {
		new ProcessUtils().processByClass(clazz, templateRootDir,true);
	}
	
	public void generateBySql(Sql sql, String templateRootDir) throws Exception {
		new ProcessUtils().processBySql(sql,templateRootDir,false);
	}

	public void deleteBySql(Sql sql,String templateRootDir) throws Exception {
		new ProcessUtils().processBySql(sql,templateRootDir,true);
	}
	
    /**
     * 根据模板路径生成返回一个Generator(代码生成器核心引擎)实例
     * @param templateRootDir
     * @return
     */
    private Generator getGenerator(String templateRootDir) {
    	//File.getAbsoluteFile() 返回抽象路径名的绝对路径名形式
        g.setTemplateRootDir(new File(templateRootDir).getAbsoluteFile());
        return g;
    }
    
    /** 生成器的上下文，存放的变量将可以在模板中引用 */
    public static class GeneratorContext {
        static ThreadLocal<Map> context = new ThreadLocal<Map>();
        public static void clear() {
            Map m = context.get();
            if(m != null) m.clear();
        }
        public static Map getContext() {
            Map map = context.get();
            if(map == null) {
                setContext(new HashMap());
            }
            return context.get();
        }
        public static void setContext(Map map) {
            context.set(map);
        }
        public static void put(String key,Object value) {
            getContext().put(key, value);
        }
    }
    
    public class ProcessUtils {
        
    	public void processByMap(Map params, String templateRootDir,boolean isDelete) throws Exception, FileNotFoundException {
			Generator g = getGenerator(templateRootDir);
			GeneratorModel m = GeneratorModelUtils.newFromMap(params);
			processByGeneratorModel(templateRootDir, isDelete, g, m);
    	}
    	
    	public void processBySql(Sql sql,String templateRootDir,boolean isDelete) throws Exception {
    		Generator g = getGenerator(templateRootDir);
    		GeneratorModel m = GeneratorModelUtils.newFromSql(sql);
    		PrintUtils.printBeginProcess("sql:"+sql.getSourceSql(),isDelete);
    		processByGeneratorModel(templateRootDir,isDelete,g,m);
    	}   
    	
    	/**
    	 * 通过指定类加工生成所需文件
    	 * @param clazz
    	 * @param templateRootDir
    	 * @param isDelete
    	 * @throws Exception
    	 * @throws FileNotFoundException
    	 */
    	public void processByClass(Class clazz, String templateRootDir,boolean isDelete) throws Exception, FileNotFoundException {
			Generator g = getGenerator(templateRootDir);
			//从传入的实体类中获取相关值分别赋予文件路径可以引用的变量，生成器模板可以引用的变量
			GeneratorModel m = GeneratorModelUtils.newFromClass(clazz);
			PrintUtils.printBeginProcess("JavaClass:"+clazz.getSimpleName(),isDelete);
			//生成文件
			processByGeneratorModel(templateRootDir, isDelete, g, m);
    	}

        private void processByGeneratorModel(String templateRootDir,
                                             boolean isDelete, Generator g,
                                             GeneratorModel m)
                                                              throws Exception,
                                                              FileNotFoundException {
            try {
				if(isDelete)
					g.deleteBy(m.templateModel, m.filePathModel);
				else
					g.generateBy(m.templateModel, m.filePathModel);
			}catch(GeneratorException ge) {
				PrintUtils.printExceptionsSumary(ge.getMessage(),getGenerator(templateRootDir).getOutRootDir(),ge.getExceptions());
			}
        }
    	
        /**
         * 通过数据库表生成文件
         * @param tableName
         * @param templateRootDir
         * @param isDelete
         * @throws Exception
         */
        public void processByTable(String tableName,String templateRootDir,boolean isDelete) throws Exception {
        	if("*".equals(tableName)) {
        	    if(isDelete)
        	        deleteByAllTable(templateRootDir);
        	    else
        	        generateByAllTable(templateRootDir);
        		return;
        	}
        	//根据模板路径生成返回一个Generator(代码生成器核心引擎)实例
    		Generator g = getGenerator(templateRootDir);
    		
    		//把数据库中的表映射到java的Table对象中
    		Table table = TableFactory.getInstance().getTable(tableName);
    		try {
    			//生成所需要的所有文件
    			processByTable(g,table,isDelete);
    		}catch(GeneratorException ge) {
    			PrintUtils.printExceptionsSumary(ge.getMessage(),getGenerator(templateRootDir).getOutRootDir(),ge.getExceptions());
    		}
    	}    
        
		public void processByAllTable(String templateRootDir,boolean isDelete) throws Exception {
			List<Table> tables = TableFactory.getInstance().getAllTables();
			List exceptions = new ArrayList();
			for(int i = 0; i < tables.size(); i++ ) {
				try {
					processByTable(getGenerator(templateRootDir),tables.get(i),isDelete);
				}catch(GeneratorException ge) {
					exceptions.addAll(ge.getExceptions());
				}
			}
			PrintUtils.printExceptionsSumary("",getGenerator(templateRootDir).getOutRootDir(),exceptions);
		}
		
		/**
		 * 根据生成器引擎，表对象，是否删除标志，生成或者删除相关文件
		 * @param g
		 * @param table
		 * @param isDelete
		 * @throws Exception
		 */
		public void processByTable(Generator g, Table table,boolean isDelete) throws Exception {
			//构造freemarker中可以引用的变量的值
	        GeneratorModel m = GeneratorModelUtils.newFromTable(table);
	        PrintUtils.printBeginProcess(table.getSqlName()+" => "+table.getClassName(),isDelete);
	        if(isDelete)
	        	g.deleteBy(m.templateModel,m.filePathModel);//删除生成的文件
	        else 	        	
	        	g.generateBy(m.templateModel,m.filePathModel);//生成文件
	    } 
    }
    
	
    @SuppressWarnings("all")
	public static class GeneratorModelUtils {
		
		/**
		 * 根据table对象中的属性把值放入GeneratorModel中
		 * @param table
		 * @return
		 */
		public static GeneratorModel newFromTable(Table table) {
			Map templateModel = new HashMap();
			templateModel.put("table", table);
			setShareVars(templateModel);
			
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			filePathModel.putAll(BeanHelper.describe(table));
			return new GeneratorModel(templateModel,filePathModel);
		}
		


		/**
		 * 根据Sql对象中的属性把值放入GeneratorModel中
		 * @param table
		 * @return
		 */
		public static GeneratorModel newFromSql(Sql sql) throws Exception {
			Map templateModel = new HashMap();
			templateModel.put("sql", sql);
			setShareVars(templateModel);
			
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			filePathModel.putAll(BeanHelper.describe(sql));
			return new GeneratorModel(templateModel,filePathModel);
		}

		/**
		 * 从传入的实体类中获取相关值分别赋予文件路径可以引用的变量(filePathModel)，生成器模板可以引用的变量(templateModel)
		 * @param clazz
		 * @return
		 * @author gwhuang
		 */
		public static GeneratorModel newFromClass(Class clazz) {
			//生成器模板可以引用的变量
			Map templateModel = new HashMap();
			templateModel.put("clazz", new JavaClass(clazz));
			setShareVars(templateModel);
			
			//文件路径可以引用的变量
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			//通过PropertyUtils的describe方法把entity类的所有属性与属性值封装进Map中
			filePathModel.putAll(BeanHelper.describe(new JavaClass(clazz)));
			return new GeneratorModel(templateModel,filePathModel);
		}
		
		public static GeneratorModel newFromMap(Map params) {
			Map templateModel = new HashMap();
			templateModel.putAll(params);
			setShareVars(templateModel);
			
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			filePathModel.putAll(params);
			return new GeneratorModel(templateModel,filePathModel);
		}
		
		/**
		 * 设置公共变量
		 * @param templateModel
		 */
		public static void setShareVars(Map templateModel) {
			templateModel.putAll(GeneratorProperties.getProperties());
			templateModel.putAll(System.getProperties());
			templateModel.put("env", System.getenv());
			templateModel.put("now", new Date());
			templateModel.putAll(GeneratorContext.getContext());
		}

	}
	
	private static class PrintUtils {
		
		private static void printExceptionsSumary(String msg,String outRoot,List<Exception> exceptions) throws FileNotFoundException {
			File errorFile = new File(outRoot,"generator_error.log");
			if(exceptions != null && exceptions.size() > 0) {
				System.err.println("[Generate Error Summary] : "+msg);
				PrintStream output = new PrintStream(new FileOutputStream(errorFile));
				for(int i = 0; i < exceptions.size(); i++) {
					Exception e = exceptions.get(i);
                    System.err.println("[GENERATE ERROR]:"+e);
					if(i == 0) e.printStackTrace();
					e.printStackTrace(output);
				}
				output.close();
				System.err.println("***************************************************************");
				System.err.println("* "+"* 输出目录已经生成generator_error.log用于查看错误 ");
				System.err.println("***************************************************************");
			}
		}
		
		private static void printBeginProcess(String displayText,boolean isDatele) {
			GLogger.println("***************************************************************");
			GLogger.println("* BEGIN " + (isDatele ? " delete by " : " generate by ")+ displayText);
			GLogger.println("***************************************************************");
		}
		
		public static void printAllTableNames(List<Table> tables) throws Exception {
			GLogger.println("\n----All TableNames BEGIN----");
			for(int i = 0; i < tables.size(); i++ ) {
				String sqlName = ((Table)tables.get(i)).getSqlName();
				GLogger.println("g.generateTable(\""+sqlName+"\");");
			}
			GLogger.println("----All TableNames END----");
		}
	}

}
