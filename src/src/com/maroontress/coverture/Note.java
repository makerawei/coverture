package com.maroontress.coverture;

import com.maroontress.coverture.gcda.DataRecord;
import com.maroontress.coverture.gcda.FunctionDataRecord;
import com.maroontress.coverture.gcno.FunctionGraphRecord;
import com.maroontress.coverture.gcno.NoteRecord;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;

/**
   gcno�ե������ѡ���������̤��ݻ����ޤ���
*/
public final class Note {

    /** gcno�ե�����Ǥ��� */
    private File file;

    /** gcno�ե�����ΥС�������ֹ�Ǥ��� */
    private int version;

    /**
       gcno�ե�����Υ����ॹ����פǤ���gcda�ե������Ʊ�����Ȥ�Ƥ�
       �뤳�Ȥ��ǧ���뤿��˻��Ѥ���ޤ���
    */
    private int stamp;

    /**
       gcno�ե�����Υե����륷���ƥ�ǤΥ����ॹ����פǤ����������ե�
       ����Υ����ॹ����פ���Ӥ��뤿��˻��Ѥ���ޤ���
    */
    private long lastModified;

    /** �ؿ�����դȤ��μ��̻ҤΥޥåפǤ��� */
    private TreeMap<Integer, FunctionGraph> map;

    /**
       �Ρ��ȥ쥳���ɤ��饤�󥹥��󥹤��������ޤ���

       @param rec �Ρ��ȥ쥳����
       @param file gcno�ե�����
       @throws CorruptedFileException �ե�����ι�¤������Ƥ��뤳�Ȥ򸡽�
    */
    private Note(final NoteRecord rec, final File file)
	throws CorruptedFileException {
	this.file = file;
	version = rec.getVersion();
	stamp = rec.getStamp();
	lastModified = file.lastModified();
	map = new TreeMap<Integer, FunctionGraph>();

	FunctionGraphRecord[] list = rec.getList();
	for (FunctionGraphRecord e : list) {
	    FunctionGraph fg = new FunctionGraph(e);
	    map.put(fg.getId(), fg);
	}
    }

    /**
       gcov�ߴ��Υ������ե�����Υ��Х�å����������ޤ���
    */
    public void createSourceList() {
	SourceList sl = new SourceList();
	Collection<FunctionGraph> allGraphs = map.values();
	for (FunctionGraph g : allGraphs) {
	    g.addLineCounts(sl);
	}

	String path = file.getPath();
	path = path.substring(0, path.lastIndexOf('.')) + ".gcov";
	sl.ouputFiles(path, lastModified);
    }

    /**
       �Ρ��Ȥ�XML�����ǽ��Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	out.printf("<note version='0x%x' stamp='0x%x' lastModified='%d'>\n",
		   version, stamp, lastModified);
	Collection<FunctionGraph> allGraphs = map.values();
	for (FunctionGraph g : allGraphs) {
	    g.printXML(out);
	}
	out.printf("</note>\n");
    }

    /**
       �ǡ����쥳���ɤ����ꤷ�ޤ���

       @param rec �ǡ����쥳����
       @param lastModified gcda�ե�����Υե����륷���ƥ�Υ����ॹ��
       ���
       @throws CorruptedFileException
    */
    private void setDataRecord(final DataRecord rec, final long lastModified)
	throws CorruptedFileException {
	if (version != rec.getVersion()) {
	    throw new CorruptedFileException("gcda file: version mismatch.");
	}
	if (stamp != rec.getStamp()) {
	    throw new CorruptedFileException("gcda file: timestamp mismatch.");
	}
	if (this.lastModified > lastModified) {
	    System.out.println("warning: gcno file is newer than gcda file.");
	}
	FunctionDataRecord[] list = rec.getList();
	for (FunctionDataRecord e : list) {
	    int id = e.getId();
	    FunctionGraph g = map.get(id);
	    if (g == null) {
		System.out.printf("warning: unknown function id '%d'.", id);
		continue;
	    }
	    g.setFunctionDataRecord(e);
	}
    }

    /**
       gcda�ե������ѡ������ơ��Ρ��Ȥ˥����������󥿤��ɲä��ޤ���
       ����ͥ��ޥåפ���Τǡ�2G�Х��Ȥ�Ķ����ե�����ϰ����ޤ���

       �ե���������Ƥ������ʾ��ϡ�ɸ�२�顼���Ϥ˥����å��ȥ졼��
       ����Ϥ��ޤ���

       @param path gcda�ե�����Υѥ�
    */
    private void parseData(final String path) {
	if (!path.endsWith(".gcda")) {
	    System.err.printf("%s: suffix is not '.gcda'.\n", path);
	    return;
	}
	File file = new File(path);
	try {
	    FileChannel ch = new RandomAccessFile(file, "r").getChannel();
	    ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY,
				   0, ch.size());
	    try {
		DataRecord dataRecord = new DataRecord(bb);
		setDataRecord(dataRecord, file.lastModified());
	    } catch (UnexpectedTagException e) {
		e.printStackTrace();
	    } catch (CorruptedFileException e) {
		e.printStackTrace();
	    } finally {
		ch.close();
	    }
	} catch (FileNotFoundException e) {
	    System.err.println(path + ": not found");
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
       gcno�ե������ѡ������ơ��Ρ��Ȥ��������ޤ�������ͥ��ޥå�
       ����Τǡ�2G�Х��Ȥ�Ķ����ե�����ϰ����ޤ���

       �ե���������Ƥ������ʾ��ϡ�ɸ�२�顼���Ϥ˥����å��ȥ졼��
       ����Ϥ��ơ�null���֤��ޤ���

       @param path gcno�ե�����Υѥ�
       @return �Ρ���
       @throws IOException
    */
    public static Note parse(final String path) throws IOException {
	if (!path.endsWith(".gcno")) {
	    System.err.printf("%s: suffix is not '.gcno'.", path);
	    return null;
	}
	File file = new File(path);
	FileChannel ch = new RandomAccessFile(file, "r").getChannel();
	ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
	Note note = null;

	try {
	    NoteRecord noteRecord = new NoteRecord(bb);
	    note = new Note(noteRecord, file);
	} catch (UnexpectedTagException e) {
	    e.printStackTrace();
	    return null;
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return null;
	} finally {
	    ch.close();
	}
	note.parseData(path.substring(0, path.lastIndexOf('.')) + ".gcda");
	return note;
    }
}
