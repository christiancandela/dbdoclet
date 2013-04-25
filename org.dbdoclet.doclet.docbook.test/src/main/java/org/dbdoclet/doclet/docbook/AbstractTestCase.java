package org.dbdoclet.doclet.docbook;

import static com.sun.tools.javac.code.Flags.PRIVATE;
import static com.sun.tools.javac.code.Flags.PROTECTED;
import static com.sun.tools.javac.code.Flags.PUBLIC;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.dbdoclet.progress.InfoListener;
import org.dbdoclet.service.ExecResult;
import org.dbdoclet.service.ExecServices;
import org.dbdoclet.xiphias.XmlServices;
import org.dbdoclet.xiphias.XmlValidationResult;
import org.dbdoclet.xiphias.XsdServices;
import org.junit.Before;
import org.w3c.dom.Document;

import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javadoc.JavadocTool;
import com.sun.tools.javadoc.Messager;
import com.sun.tools.javadoc.ModifierFilter;
import com.sun.tools.javadoc.RootDocImpl;

public class AbstractTestCase implements InfoListener {

	protected static final ResourceBundle res = ResourceBundle
			.getBundle("org/dbdoclet/doclet/docbook/Resources");
	private final String docbookXsdFileName = "src/main/resources/xsd/docbook.xsd";

	protected String destPath = "build/test";
	protected String sourcePath = "src/test/java/";

	@Override
	public void info(String text) {
		System.out.println(text);
	}

	@Before
	public void startUp() {
	}

	protected RootDocImpl javadoc(String[] sources, String classpath,
			String[][] args) {

		Context context = new Context();
		Messager.preRegister(context, "dbdoclet");

		JavadocTool tool = JavadocTool.make0(context);

		final ListBuffer<String> subPackages = new ListBuffer<String>();
		subPackages.append("yes");

		final ListBuffer<String> xcludePackages = new ListBuffer<String>();

		final ListBuffer<String> javaNames = new ListBuffer<String>();

		for (String srcpath : sources) {
			javaNames.append(srcpath);
		}

		final ListBuffer<String[]> options = new ListBuffer<String[]>();
		options.append(new String[] { "-d", destPath });

		if (args != null) {

			for (String[] arg : args) {
				options.append(arg);
			}
		}
		// final Options compOpts = Options.instance(context);
		// compOpts.put("-classpath", classpath);
		// compOpts.put("-d", destPath);

		RootDocImpl root;

		try {
			root = tool.getRootDocImpl("de", "", new ModifierFilter(PUBLIC
					| PROTECTED | PRIVATE), javaNames.toList(),
					options.toList(), false, subPackages.toList(),
					xcludePackages.toList(), false, false, false);
		} catch (IOException oops) {
			oops.printStackTrace();
			return null;
		}

		return root;
	}

	protected void pln(String text) {
		System.out.println(text);
	}

	protected void runForked(String cmd) {
		try {

			ExecResult rc = ExecServices.exec(cmd, this);
			assertTrue(rc.failed() == false);

			XmlValidationResult result = XsdServices.validate(new File(destPath
					+ "Reference.xml"), new File(docbookXsdFileName));

			if (result.failed()) {
				System.out.println(result.createTextReport());
			}

			assertTrue(result.failed() == false);

		} catch (Exception oops) {

			oops.printStackTrace();
			fail(oops.getMessage());
		}
	}

	protected String xpath(String query) {

		try {

			Document doc = XmlServices.parse(new File(destPath
					+ "/Reference.xml"));

			Object obj = null;

			JXPathContext context = JXPathContext.newContext(doc);
			context.registerNamespace("db", "http://docbook.org/ns/docbook");
			CompiledExpression expr = JXPathContext.compile(query);

			obj = expr.getValue(context);

			if (obj == null) {
				return "";
			}

			return obj.toString();

		} catch (Exception oops) {

			oops.printStackTrace();
			fail(oops.getMessage());
		}

		return "";
	}

}
