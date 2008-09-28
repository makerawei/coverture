package com.maroontress.coverture;

import java.io.BufferedReader;
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

    /** �Хåե��Υ����� */
    private static final int BUFFER_SIZE = 4096;

    /** */
    private boolean outputGcov;

    /** */
    private String inputFile;

    /** */
    private Charset sourceFileCharset;

    /** */
    private String[] files;

    /**
       ��ư���饹�Υ��󥹥��󥹤��������ޤ���
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

	opt.add("input-file", new OptionListener() {
	    public void run(final String name, final String arg) {
		inputFile = arg;
	    }
	}, "FILE", "Read the list of files from %s.");

	opt.add("source-file-charset", "CHARSET",
		"Specify the charset of source files.");

	opt.add("gcov", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputGcov = true;
	    }
	}, "Output .gcov files compatible with gcov.");

	sourceFileCharset = Charset.defaultCharset();
	try {
	    files = opt.parse(av);
	    String csn = opt.getValue("source-file-charset");
	    if (csn != null) {
		try {
		    sourceFileCharset = Charset.forName(csn);
		} catch (IllegalArgumentException e) {
		    String m = "Unsupported charset: " + csn;
		    throw new OptionsParsingException(m);
		}
	    }
	} catch (OptionsParsingException e) {
	    System.out.println(e.getMessage());
	    usage(opt);
	}
	if (files.length == 0 && inputFile == null) {
	    usage(opt);
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
	    note.createSourceList(sourceFileCharset);
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
