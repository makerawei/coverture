package com.maroontress.coverture;

import com.maroontress.cui.OptionListener;
import com.maroontress.cui.Options;
import com.maroontress.cui.OptionsParsingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
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

    /** �إ�ץ�å������Υ���ǥ�����Ǥ��� */
    private static final int INDENT_WIDTH = 32;

    /** �ǥե���ȤΥ���åɤθĿ��� */
    private static final int DEFAULT_THREADS = 4;

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

    /** ���ޥ�ɥ饤�󥪥ץ���������Ǥ��� */
    private Options options;

    /**
       ��ư���饹�Υ��󥹥��󥹤��������ޤ���

       @param av ���ޥ�ɥ饤�󥪥ץ���������
    */
    private Coverture(final String[] av) {
	threads = DEFAULT_THREADS;
	ioProperties = new IOProperties();

	options = new Options();
	options.add("help", new OptionListener() {
	    public void run(final String name, final String arg) {
		usage();
	    }
	}, "Show this message and exit.");

	options.add("version", new OptionListener() {
	    public void run(final String name, final String arg) {
		version();
	    }
	}, "Show version and exit.");

	options.add("output-dir", new OptionListener() {
	    public void run(final String name, final String arg) {
		ioProperties.setOutputDir(new File(arg));
	    }
	}, "DIR", "Specify where to place generated files.");

	options.add("input-file", new OptionListener() {
	    public void run(final String name, final String arg) {
		inputFile = arg;
	    }
	}, "FILE", "Read the list of files from FILE:\n"
		    + "FILE can be - for standard input.");

	options.add("source-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		ioProperties.setSourceFileCharset(getCharset(arg));
	    }
	}, "CHARSET", "Specify the charset of source files.");

	options.add("gcov", new OptionListener() {
	    public void run(final String name, final String arg) {
		outputGcov = true;
	    }
	}, "Output .gcov files compatible with gcov.");

	options.add("gcov-file-charset", new OptionListener() {
	    public void run(final String name, final String arg)
		throws OptionsParsingException {
		ioProperties.setGcovFileCharset(getCharset(arg));
	    }
	}, "CHARSET", "Specify the charset of .gcov files.");

	options.add("threads", new OptionListener() {
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
		    + "NUM > 0; 4 is the default.");

	options.add("verbose", new OptionListener() {
	    public void run(final String name, final String arg) {
		ioProperties.setVerbose(true);
	    }
	}, "Be extra verbose.");

	try {
	    files = options.parse(av);
	} catch (OptionsParsingException e) {
	    System.err.println(e.getMessage());
	    usage();
	}
	if (files.length == 0 && inputFile == null) {
	    usage();
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
	throws OptionsParsingException {
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
	    InputStreamReader in;
	    if (inputFile.equals("-")) {
		in = new InputStreamReader(System.in);
	    } else {
		in = new FileReader(inputFile);
	    }
	    BufferedReader rd = new BufferedReader(in);
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
	    out.print("<gcno>\n");
	    for (Note note : set) {
		note.printXML(out);
	    }
	    out.print("</gcno>\n");
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
       ������ˡ��ɽ�����ޤ���

       @param out ���ϥ��ȥ꡼��
    */
    private void printUsage(final PrintStream out) {
        out.printf("Usage: coverture [Options] [FILE...]%n"
                   + "Options are:%n");
        String[] help = options.getHelpMessage(INDENT_WIDTH).split("\n");
        for (String s : help) {
            out.printf("  %s%n", s);
        }
    }

    /**
       ������ˡ��ɽ�����ƽ�λ���ޤ���
    */
    private void usage() {
        printUsage(System.err);
        System.exit(1);
    }

    /**
       �С���������Ϥ��ƽ�λ���ޤ���
    */
    private void version() {
	BufferedReader in = new BufferedReader(
	    new InputStreamReader(getClass().getResourceAsStream("version")));
        try {
	    String s;
            while ((s = in.readLine()) != null) {
                System.out.println(s);
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
        System.exit(0);
    }
}
