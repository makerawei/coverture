package com.maroontress.coverture;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
   Coverture�ε�ư���饹�Ǥ���
*/
public final class Coverture {

    /** �Хåե��Υ����� */
    private static final int BUFFER_SIZE = 4096;

    /**
       ��ư���饹�Υ��󥹥��󥹤��������ޤ���
    */
    private Coverture() {
    }

    /**
       ������ˡ��ɽ�����ƽ�λ���ޤ���
    */
    private static void usage() {
        System.err.print(""
+ "Usage: java com.maroontress.coverture.Coverture [options] [file...]\n"
+ "Options are:\n"
+ "  --input-file=FILE       Read the list of files from FILE.\n"
+ "  --version               Show version and exit.\n"
+ "");
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
       �ե����뤫��gcno�ե�����̾�Υꥹ�Ȥ����Ϥ�������gcno�ե������
       �������ޤ���

       @param inputFile ���Ϥ���ե�����̾
       @param out ������
       @throws IOException �����ϥ��顼
       @throws CorruptedFileException �ե�����ι�¤������Ƥ��뤳�Ȥ򸡽�
       @throws UnexpectedTagException ͽ�����ʤ������򸡽�
    */
    private static void processFileList(final String inputFile,
					final PrintWriter out)
	throws IOException, CorruptedFileException, UnexpectedTagException {
	try {
	    BufferedReader rd = new BufferedReader(new FileReader(inputFile));
	    String name;
	    while ((name = rd.readLine()) != null) {
		Note note = Note.parse(name);
		note.printXML(out);
	    }
	} catch (FileNotFoundException e) {
	    System.err.println("File not found: " + e.getMessage());
	    System.exit(1);
	}
    }

    /**
       Coverture��¹Ԥ��ޤ���

       @param av ���ޥ�ɥ饤�󥪥ץ����
    */
    public static void main(final String[] av) {
        ArgumentParser ap = new ArgumentParser(av);
        Argument arg;
	String inputFile = null;

        while ((arg = ap.getArgument()) != null && arg.isOption()) {
            String name = arg.getName();
            String value = arg.getValue();
            if (name.equals("--version")) {
                version();
            } else if (name.equals("--input-file") && value != null) {
                inputFile = value;
            } else {
                usage();
            }
        }
	if (arg == null && inputFile == null) {
	    usage();
	}
	try {
	    PrintWriter out = new PrintWriter(System.out);
	    out.println("<gcno>");
	    for (; arg != null; arg = ap.getArgument()) {
		Note note = Note.parse(arg.getName());
		if (note == null) {
		    continue;
		}
		note.printXML(out);
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
}
