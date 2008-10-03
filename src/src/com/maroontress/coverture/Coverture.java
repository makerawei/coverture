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
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
   Coverture�ε�ư���饹�Ǥ���
*/
public final class Coverture {

    /** �Хåե��Υ������Ǥ��� */
    private static final int BUFFER_SIZE = 4096;

    /** �ǥե���ȤΥ���åɤθĿ��� */
    private static final int DEFAULT_THREADS = 4;

    /** �إ�ץ�å������Υ���ǥ�Ȥο����Ǥ��� */
    private static final int HELP_INDENT_COUNT = 36;

    /** gcov�ե��������Ϥ��뤫�ɤ����Υե饰�Ǥ��� */
    private boolean outputGcov;

    /** gcno�ե�����Υꥹ�ȥե�����Υѥ��Ǥ��� */
    private String inputFile;

    /** �����ϥץ�ѥƥ��Ǥ��� */
    private IOProperties ioProperties;

    /** ���ޥ�ɥ饤��ǻ��ꤵ�줿gcno�ե�����Υѥ�������Ǥ��� */
    private String[] files;

    /**
       ��������ե�����θĿ��Ǥ���files.length��--input-file�ǻ��ꤷ
       ���ե�����ꥹ�ȤθĿ���ä�����Τˤʤ�ޤ���
    */
    private int taskCount;

    /** Note���󥹥��󥹤�����������Ʊ���������Υ��塼�Ǥ��� */
    private CompletionService<Note> service;

    /** gcno�ե������ѡ������륹��åɤθĿ��Ǥ��� */
    private int threads;

    /**
       ��ư���饹�Υ��󥹥��󥹤��������ޤ���

       @param av ���ޥ�ɥ饤�󥪥ץ���������
    */
    private Coverture(final String[] av) {
	final Options opt = new Options();
	String helpIndent = "";
	for (int k = 0; k < HELP_INDENT_COUNT; ++k) {
	    helpIndent += " ";
	}
	threads = DEFAULT_THREADS;
	ioProperties = new IOProperties();

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
		ioProperties.setOutputDir(new File(arg));
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
		ioProperties.setSourceFileCharset(getCharset(arg));
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
		ioProperties.setGcovFileCharset(getCharset(arg));
	    }
	}, "CHARSET", "Specify the charset of .gcov files.");

	opt.add("threads", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		String m = "invalid value: " + arg;
		int num;
		try {
		    num = Integer.valueOf(arg);
		} catch (NumberFormatException e) {
		    throw new OptionsParsingException(m);
		}
		if (num <= 0) {
		    throw new OptionsParsingException(m);
		}
		threads = num;
	    }
	}, "NUM", "Specify the number of parser threads:\n"
		+ helpIndent + "NUM > 0; 4 is the default.");

	try {
	    files = opt.parse(av);
	} catch (OptionsParsingException e) {
	    System.err.println(e.getMessage());
	    usage(opt);
	}
	if (files.length == 0 && inputFile == null) {
	    usage(opt);
	}
	service = new ExecutorCompletionService<Note>(
	    Executors.newFixedThreadPool(threads));
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
    */
    private void processFile(final String name) {
	++taskCount;
	service.submit(new Callable<Note>() {
	    public Note call() throws Exception {
		Note note = Note.parse(name);
		if (note == null) {
		    return null;
		}
		if (outputGcov) {
		    note.createSourceList(ioProperties);
		}
		return note;
	    }
	});
    }

    /**
       �ե����뤫��gcno�ե�����̾�Υꥹ�Ȥ����Ϥ�������gcno�ե������
       �������ޤ���

       @param inputFile ���Ϥ���ꥹ�ȤΥե�����̾
       @throws IOException �����ϥ��顼
    */
    private void processFileList(final String inputFile) throws IOException {
	try {
	    BufferedReader rd = new BufferedReader(new FileReader(inputFile));
	    String name;
	    while ((name = rd.readLine()) != null) {
		processFile(name);
	    }
	} catch (FileNotFoundException e) {
	    System.err.printf("%s: not found: %s", inputFile, e.getMessage());
	    System.exit(1);
	}
    }

    /**
       ���ꤵ�줿�ե�����������Ϥ�¹Ԥ��ޤ���
    */
    private void run() {
	try {
	    if (outputGcov) {
		ioProperties.makeOutputDir();
	    }
	    for (String arg : files) {
		processFile(arg);
	    }
	    if (inputFile != null) {
		processFileList(inputFile);
	    }

	    TreeSet<Note> set = new TreeSet<Note>(Note.getOriginComparator());
	    for (int k = 0; k < taskCount; ++k) {
		Future<Note> future = service.take();
		Note note = future.get();
		if (note == null) {
		    continue;
		}
		set.add(note);
	    }
	    PrintWriter out = new PrintWriter(System.out);
	    out.println("<gcno>");
	    for (Note note : set) {
		note.printXML(out);
	    }
	    out.println("</gcno>");
	    out.close();
	} catch (ExecutionException e) {
	    e.getCause().printStackTrace();
	    System.exit(1);
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
