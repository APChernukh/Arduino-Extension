package arduino;

import java.util.HashMap;
import java.util.Vector;

import jssc.SerialPort;
import jssc.SerialPortException;

import org.nlogo.api.Argument;

import org.nlogo.api.Context;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionManager;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.Syntax;


public class ArduinoExtension extends DefaultClassManager {

	static SerialPort serialPort;
	static PortListener portListener;
	static int BAUD_RATE = 9600;
	
	static public HashMap<String,Double> values = new HashMap<String,Double>();
	static {
		values.put("BaudRate", (double) BAUD_RATE);
	}
	
	static public double get(String key) {
		String lcKey = key.toLowerCase();
		if ( values.containsKey( lcKey ) )  {
			return values.get(lcKey);
		} else {
			return Double.NaN;
		}
	}
	
	@Override
	public void load(PrimitiveManager pm) throws ExtensionException {
		pm.addPrimitive("primitives", new Primitives());
		pm.addPrimitive("ports", new Ports() );
		pm.addPrimitive("open", new Open() );
		pm.addPrimitive("close", new Close() );
		pm.addPrimitive("get", new Get() );
		pm.addPrimitive("write-string", new WriteString() );
		pm.addPrimitive("write-int", new WriteInt() );
		pm.addPrimitive("write-byte", new WriteByte() );
		pm.addPrimitive("is-open?", new IsOpen());
	}
	
	
	
	public static class Primitives extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(Syntax.ListType());
	    }
		
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			
			LogoListBuilder llist = new LogoListBuilder();
			String[] prims = {"reporter:primitives", "reporter:ports", 
					"reporter:get[Name:String(case-insensitive)]", "reporter:is-open?",
					"",
					"command:open[Port:String]", "command:close", 
					"command:write-string[Message:String]",
					"command:write-int[Message:int]", 
					"command:write-byte[Message:byte]",
					"",
					"ALSO NOTE: Baud Rate of 9600 is expected"};
			for (String prim : prims ) {
				llist.add(prim);
			}
			return llist.toLogoList();
		}
	}
	
	public static class Ports extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(Syntax.ListType());
	    }
		
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			
			LogoListBuilder llist = new LogoListBuilder();
			String[] names = jssc.SerialPortList.getPortNames();
			for (String name : names ) {
				llist.add(name);
			}
			return llist.toLogoList();
		}
	}
	
	
	public static class Open extends DefaultCommand {

		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.StringType() });
		}
		@Override
		public void perform(Argument[] args, Context ctxt)
				throws ExtensionException, LogoException {

			if ( serialPort != null && serialPort.isOpened() ) {
				throw new ExtensionException("Port is already open");
			} else {
				try {
					serialPort = new SerialPort(args[0].getString());
					serialPort.openPort();
					serialPort.setParams(BAUD_RATE, 8, 1, 0);
		            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
		            serialPort.setEventsMask(mask); //Set mask
					portListener = new PortListener( serialPort );
					serialPort.addEventListener(portListener);
				} catch (SerialPortException e) {
					throw new ExtensionException("Error in opening port: " + e.getMessage());
				}
			}
		}
	}
	
	
	public static class IsOpen extends DefaultReporter {
		@Override
		public Object report(Argument[] arg0, Context arg1)
				throws ExtensionException, LogoException {
			return (serialPort != null && serialPort.isOpened() && portListener != null);
		}
	}
	
	
	public static void doClose() throws ExtensionException {
		try {
			serialPort.removeEventListener();
			serialPort.closePort();
		} catch (SerialPortException e) {
			throw new ExtensionException( "Error in writing: " + e.getMessage() );
		}
	}
	
	public static class Close extends DefaultCommand {
		@Override
		public void perform(Argument[] args, Context ctxt)
				throws ExtensionException, LogoException {
			if ( (serialPort == null) || (!serialPort.isOpened()) ) {
				throw new ExtensionException( "Serial Port not Open");
			} else {
				doClose();
			}
		}
	}
	
	
	public static class Get extends DefaultReporter {
		@Override
		public Syntax getSyntax() {
	      return Syntax.reporterSyntax(new int[] {Syntax.StringType()}, Syntax.NumberType());
	    }
		
		@Override
		public Object report(Argument[] args, Context ctxt)
				throws ExtensionException, LogoException {
			return get(args[0].getString());
		}
	}
	
	public static class WriteString extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.StringType() });
		}
		@Override
		public void perform(Argument[] args, Context ctxt)
				throws ExtensionException, LogoException {
			if ( (serialPort == null) || (!serialPort.isOpened()) ) {
				throw new ExtensionException( "Serial Port not Open");
			}
			try {
				serialPort.writeString(args[0].getString());
			} catch (SerialPortException e) {
				throw new ExtensionException( "Error in writing: " + e.getMessage() );
			}
		}
	}
	
	public static class WriteInt extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.NumberType() });
		}
		@Override
		public void perform(Argument[] args, Context ctxt)
				throws ExtensionException, LogoException {
			if ( (serialPort == null) || (!serialPort.isOpened()) ) {
				throw new ExtensionException( "Serial Port not Open");
			}
			try {
				serialPort.writeInt(args[0].getIntValue());
			} catch (SerialPortException e) {
				throw new ExtensionException( "Error in writing: " + e.getMessage() );
			}
		}
	}
	
	public static class WriteByte extends DefaultCommand {
		@Override
		public Syntax getSyntax() {
			return Syntax.commandSyntax(new int[] {Syntax.NumberType() });
		}
		@Override
		public void perform(Argument[] args, Context ctxt)
				throws ExtensionException, LogoException {
			if ( (serialPort == null) || (!serialPort.isOpened()) ) {
				throw new ExtensionException( "Serial Port not Open");
			}
			try {
				serialPort.writeByte((byte)(args[0].getIntValue()));
			} catch (SerialPortException e) {
				throw new ExtensionException( "Error in writing: " + e.getMessage() );
			}
		}
	}
	
	
	
	
	
	
	
	
	@Override
	public void unload( ExtensionManager em) {
		//first remove the event listener (if any) and close the port
		if (portListener != null && serialPort != null) {
			try {
				serialPort.removeEventListener();
			} catch (SerialPortException e) {
				e.printStackTrace();
			}
			try {
				serialPort.closePort();
			} catch (SerialPortException e2) {
				e2.printStackTrace();
			}
		} else {
			if (serialPort != null && serialPort.isOpened()) {
				try {
					serialPort.closePort();
				} catch (SerialPortException e) {
					e.printStackTrace();
				}
			}
		}
		//now unload the native library, so that if we're loading the extension
		//again on the same NetLogo run, it doesn't cause us troubles.
		try
		{
			ClassLoader classLoader = this.getClass().getClassLoader() ;
			java.lang.reflect.Field field = ClassLoader.class.getDeclaredField( "nativeLibraries" ) ;
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			Vector<Object> libs = (Vector<Object>) (field.get(classLoader)) ;
			for ( Object o : libs ) 
			{
				java.lang.reflect.Method finalize = o.getClass().getDeclaredMethod( "finalize" , new Class[0] ) ;
				finalize.setAccessible( true ) ;
				finalize.invoke( o , new Object[0] ) ;
			}
		}
		catch( Exception e )
		{
			System.err.println( e.getMessage() ) ;
		}
	}
	

}
