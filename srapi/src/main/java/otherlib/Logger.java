package otherlib;

import static org.apache.commons.io.comparator.LastModifiedFileComparator.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;


public class Logger {
	
	public static class Scope {
		private static Scope[] scopes = new Scope[0];
		
		public static String[] isScope(String scope) {
			for(int i=0; i<scopes.length; i++) {
				String[] scs = scopes[i].getScopes();
				if(scs[0].equals(scope))
					return scs;
			}
			return null;
		}
		
		private String[] scope;
		
		public Scope(String[] scopes) {
			this.scope = scopes;
			Scope.scopes = add(Scope.scopes, this);
		}
		public String[] getScopes() {
			return scope;
		}
		
		private static Scope[] add(Scope[] arr, Scope item) {
			Scope[] arr2 = new Scope[arr.length + 1];
			System.arraycopy(arr, 0, arr2, 0, arr.length);
			arr2[arr.length] = item;
			return arr2;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Scope && Arrays.equals(scope, ((Scope) obj).getScopes())) 
				return true;
			return false;
		}
	}

	public static final Scope all = new Scope("all general srapi".split(" "));
	public static final Scope general = new Scope("general runerr".split(" "));
	public static final Scope runerr = new Scope("runerr".split(" "));
	public static final Scope srapi = new Scope("srapi".split(" "));
	
	
	
	public static class Type {
		public final String con;
		public Type(String con) {
			this.con = con;
		}
	}
	
	public static final List<String> severty = Arrays.asList("info warn error fatal".split(" "));
	private static int min_severty = 0;
	
	public static void setMinSeverty(Type type) {
		min_severty = severty.indexOf(type.con);
	}
	
	public static int getSevertyOf(Type type) {
		return severty.indexOf(type.con);
	}
	
	public static final Type info = new Type(severty.get(0));
	public static final Type warn = new Type(severty.get(1));
	public static final Type error = new Type(severty.get(2));
	public static final Type fatal = new Type(severty.get(3));
	
	
	private static JsonArray scopes = new JsonArray();

	private static FileOutputStream logFile = null;
	
	private static int maxDebugFiles = 5;
	
	public static void setOutputDirectory(String path) throws FileNotFoundException {
		File[] files = new File(path).listFiles();
		if(files != null && files.length >= maxDebugFiles) {
			Arrays.sort(files, LASTMODIFIED_COMPARATOR);
			files[0].delete();
		}

		path = path.replace("\\", "/") + "/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".txt";
		logFile = new FileOutputStream(new File(path));
	}
	
	
	synchronized public static void addScope(String scope) {
		
		String[] scs = Scope.isScope(scope);
		if(scs == null) {
			System.err.println("[" + info.con + "] \"" + scope + "\" is not a scope");
			return;
		}
		
		for(int i=0; i<scs.length; i++) {
			if(!scopes.contains(new JsonPrimitive(scs[i]))) {
				scopes.add(scs[i]);
				
				if(logFile == null)
					System.out.println("[" + info.con + "] added scope " + scs[i]);
				else
					write("[" + info.con + "] added scope " + scs[i]);
			}
		}
	}
	
	
	
	public static interface LoggerEventHandler {
		public default void onPrintLine(String pre, String msg, Scope scope, Type type, boolean forced) {
			System.out.println(concat(pre, msg));
		};
		public default void onPrintException(String pre, String msg, Exception e, Scope scope, Type type, boolean forced) {
			System.err.println(concat(pre, msg, "\n", except2Str(e)));
		};
		public default void onWriteLine(String pre, String msg, Scope scope, Type type, boolean forced) {
			write(concat(pre, msg));
		};
		public default void onWriteException(String pre, String msg, Exception e, Scope scope, Type type, boolean forced) {
			write(concat(pre, msg, "\n", except2Str(e)));
		};
	}
	
	private static LoggerEventHandler deh = new LoggerEventHandler() {};
	
	public static void setDebugEventHandler(LoggerEventHandler deh) {
		Logger.deh = deh;
	}
	
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	private static String getPre(Scope scope, Type type, String... add) {
		LocalDateTime now = LocalDateTime.now();
		StringBuilder sb = new StringBuilder("[")
				.append(type.con).append("] [")
				.append(now.format(formatter)).append("] [")
				.append(scope.getScopes()[0]).append("] ");
		
		for(int i=0; i<add.length; i++)
			sb.append("[").append(add[i]).append("] ");
		
		return sb.toString();
	}
	
	public static String print(String msg, Scope scope, Type type, String... add) {
		return print(msg, scope, type, false, add);
	}
	
	synchronized public static String print(String msg, Scope scope, Type type, boolean force, String... add) {
		if(should(scope, type) || force) {
			String pre = getPre(scope, type, add);
			if(logFile == null)
				deh.onPrintLine(pre, msg.replace("\n", "\n\u0009"), scope, type, force);
			else
				deh.onWriteLine(pre, msg, scope, type, force);
		}
		return msg;
	}
	
	public static void printException(String msg, Exception e, Scope scope, Type type, String... add) {
		printException(msg, e, scope, type, false, add);
	}
	
	synchronized public static void printException(String msg, Exception e, Scope scope, Type type, boolean force, String... add) {
		if(should(scope, type) || force) {
			String pre = getPre(scope, type, add);
			if(logFile == null)
				deh.onPrintException(pre, msg, e, scope, type, force);
			else
				deh.onWriteException(pre, msg, e, scope, type, force);
		}
	}
	
	
	private static boolean should(Scope scope, Type type) {
		if(scope == null || type == null)
			return false;
		
		if(severty.indexOf(type.con) < min_severty)
			return false;
		
		String mscope = scope.getScopes()[0];
		if(Logger.scopes.contains(new JsonPrimitive(mscope))) {
			return true;
		}
		return false;
	}
	
	
	private static void write(String msg) {
		try {
			logFile.write(concat(msg, "\n").getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void close() {
		try {
			logFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String except2Str(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
	
	public static String concat(Object... in) {
		StringBuilder ret = new StringBuilder();
		for(int i=0; i<in.length; i++)
			ret.append(in[i]);
		return ret.toString();
	}
	
}
