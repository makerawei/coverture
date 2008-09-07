package com.maroontress.coverture;

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
       @param lastModified gcno�ե�����Υե����륷���ƥ�Υ����ॹ��
       ���
       @throws CorruptedFileException
    */
    private Note(final NoteRecord rec, final long lastModified)
	throws CorruptedFileException {
	version = rec.getVersion();
	stamp = rec.getStamp();
	this.lastModified = lastModified;
	map = new TreeMap<Integer, FunctionGraph>();

	FunctionGraphRecord[] list = rec.getList();
	for (FunctionGraphRecord e : list) {
	    FunctionGraph fg = new FunctionGraph(e);
	    map.put(fg.getId(), fg);
	}
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
       gcno�ե������ѡ������ơ��Ρ��Ȥ��������ޤ�������ͥ��ޥå�
       ����Τǡ�2G�Х��Ȥ�Ķ����ե�����ϰ����ޤ���

       �ե���������Ƥ������ʾ��ϡ�ɸ�२�顼���Ϥ˥����å��ȥ졼��
       ����Ϥ��ơ�null���֤��ޤ���

       @param path gcno�ե�����Υѥ�
       @return �Ρ���
    */
    public static Note parse(final String path)
	throws IOException, CorruptedFileException, UnexpectedTagException {
	File file = new File(path);
	FileChannel ch = new RandomAccessFile(file, "r").getChannel();
	ByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
	Note note = null;

	try {
	    NoteRecord noteRecord = new NoteRecord(bb);
	    note = new Note(noteRecord, file.lastModified());
	} catch (UnexpectedTagException e) {
	    e.printStackTrace();
	    return null;
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return null;
	} finally {
	    ch.close();
	}
	return note;
    }
}
