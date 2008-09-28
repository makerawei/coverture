package com.maroontress.coverture;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

/**
   Coverture�ε�ư���饹�Ǥ���
*/
public final class Coverture {

    /** �Хåե��Υ������Ǥ��� */
    private static final int BUFFER_SIZE = 4096;

    /** gcov�ե��������Ϥ��뤫�ɤ����Υե饰�Ǥ��� */
    private boolean outputGcov;

    /** �ե��������Ϥ���ǥ��쥯�ȥ�Ǥ��� */
    private File outputDir;

    /** gcno�ե�����Υꥹ�ȥե�����Υѥ��Ǥ��� */
    private String inputFile;

    /** �������ե������ʸ������Ǥ��� */
    private Charset sourceFileCharset;

    /** gcno�ե������ʸ������Ǥ��� */
    private Charset gcovFileCharset;

    /**���ޥ�ɥ饤��ǻ��ꤵ�줿gcno�ե�����Υѥ�������Ǥ��� */
    private String[] files;

    /**
       ��ư���饹�Υ��󥹥��󥹤��������ޤ���

       @param av ���ޥ�ɥ饤�󥪥ץ���������
    */
    private Coverture(final String[] av) {
	final Options opt = new Options();

	opt.add("help", new OptionListener() {
	    public void run(final String name, final String arg) {
		usage(opt);
	    }
	}, "Show this message and exit.");

	opt.add("version", new OptionListener() {
	    public void run(final String name, final String arg) {
		version();
	    }
	}, "Show version and exit.");

	opt.add("output-dir", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputDir = new File(arg);
	    }
	}, "DIR", "Specify where to place generated files.");

	opt.add("input-file", new OptionListener() {
	    public void run(final String name, final String arg) {
		inputFile = arg;
	    }
	}, "FILE", "Read the list of files from %s.");

	opt.add("source-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		sourceFileCharset = getCharset(arg);
	    }
	}, "CHARSET", "Specify the charset of source files.");

	opt.add("gcov", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputGcov = true;
	    }
	}, "Output .gcov files compatible with gcov.");

	opt.add("gcov-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		gcovFileCharset = getCharset(arg);
	    }
	}, "CHARSET", "Specify the charset of .gcov files.");

	outputDir = new File(".");
	sourceFileCharset = Charset.defaultCharset();
	gcovFileCharset = Charset.defaultCharset();
	try {
	    files = opt.parse(av);
	} catch (OptionsParsingException e) {
	    System.out.println(e.getMessage());
	    usage(opt);
	}
	if (files.length == 0 && inputFile == null) {
	    usage(opt);
	}
    }

    /**
       ʸ�������������ޤ���

       csn��null�ξ��ϥǥե���Ȥ�ʸ��������֤��ޤ���

       @param csn ʸ������̾���ޤ���null
       @return ʸ������
       @throws OptionsParsingException �����ʸ������̾����ѤǤ��ʤ�
    */
    private Charset getCharset(final String csn)
	throws OptionsParsingException{
	if (csn == null) {
	    return Charset.defaultCharset();
	}
	try {
	    return Charset.forName(csn);
	} catch (IllegalArgumentException e) {
	    throw new OptionsParsingException("Unsupported charset: " + csn);
	}
    }
	
    /**
       gcno�ե������ҤȤĽ������ޤ���

       @param name ���Ϥ���gcno�ե�����Υե�����̾
       @param out ������
       @throws IOException �����ϥ��顼
       @throws CorruptedFileException �ե�����ι�¤������Ƥ��뤳�Ȥ򸡽�
    */
    private void processFile(final String name, final PrintWriter out)
	throws IOException, CorruptedFileException {
	Note note = Note.parse(name);
	if (note == null) {
	    return;
	}
	note.printXML(out);
	if (outputGcov) {
	    outputDir.mkdirs();
	    note.createSourceList(sourceFileCharset,
				  outputDir, gcovFileCharset);
	}
    }

    /**
       �ե����뤫��gcno�ե�����̾�Υꥹ�Ȥ����Ϥ�������gcno�ե������
       �������ޤ���

       @param inputFile ���Ϥ���ꥹ�ȤΥե�����̾
       @param out ������
       @throws IOException �����ϥ��顼
       @throws CorruptedFileException �ե�����ι�¤������Ƥ��뤳�Ȥ򸡽�
    */
    private void processFileList(final String inputFile, final PrintWriter out)
	throws IOException, CorruptedFileException {
	try {
	    BufferedReader rd = new BufferedReader(new FileReader(inputFile));
	    String name;
	    while ((name = rd.readLine()) != null) {
		processFile(name, out);
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("File not found: " + e.getMessage());
	    System.exit(1);
	}
    }

    /**
       ���ꤵ�줿�ե�����������Ϥ�¹Ԥ��ޤ���
    */
    private void run() {
	try {
	    PrintWriter out = new PrintWriter(System.out);
	    out.println("<gcno>");
	    for (String arg : files) {
		processFile(arg, out);
	    }
	    if (inputFile != null) {
		processFileList(inputFile, out);
	    }
	    out.println("</gcno>");
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	System.exit(0);
    }

    /**
       ������ˡ��ɽ�����ƽ�λ���ޤ���

       @param opt ���ޥ�ɥ饤�󥪥ץ��������
    */
    private static void usage(final Options opt) {
        System.err.print(""
+ "Usage: java com.maroontress.coverture.Coverture [options] [file...]\n"
+ "Options are:\n");
	Set<Map.Entry<String, String>> set = opt.getHelpMap().entrySet();
	for (Map.Entry<String, String> e : set) {
	    System.err.printf("  --%-30s  %s\n", e.getKey(), e.getValue());
	}
        System.exit(1);
    }

    /**
       �С���������Ϥ��ƽ�λ���ޤ���
    */
    private static void version() {
        InputStream in = Coverture.class.getResourceAsStream("version");
        byte[] data = new byte[BUFFER_SIZE];
        int size;
        try {
            while ((size = in.read(data)) > 0) {
                System.out.write(data, 0, size);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(1);
    }

    /**
       Coverture��¹Ԥ��ޤ���

       @param av ���ޥ�ɥ饤�󥪥ץ����
    */
    public static void main(final String[] av) {
	Coverture cov = new Coverture(av);
	cov.run();
    }
}
