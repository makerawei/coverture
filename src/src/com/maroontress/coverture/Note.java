package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractNote;
import com.maroontress.gcovparser.CorruptedFileException;
import com.maroontress.gcovparser.gcno.FunctionGraphRecord;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Comparator;

/**
   gcno�ե������ѡ���������̤��ݻ����ޤ���
*/
public final class Note extends AbstractNote<FunctionGraph> {

    /** ��Ϣ���륽���������ɤΥꥹ�ȤǤ��� */
    private SourceList sourceList;

    /**
       ���󥹥��󥹤��������ޤ���

       @param path gcno�ե�����Υѥ�
    */
    private Note(final String path) {
	super(path);
	sourceList = new SourceList();
    }

    /** {@inheritDoc} */
    @Override protected FunctionGraph createFunctionGraph(
	final FunctionGraphRecord e) throws CorruptedFileException {
	return new FunctionGraph(e);
    }

    /**
       gcov�ߴ��Υ������ե�����Υ��Х�å����������ޤ���

       @param prop �����ϥץ�ѥƥ�
    */
    public void createSourceList(final IOProperties prop) {
	sourceList.outputFiles(getOrigin(), getRuns(), getPrograms(), prop);
    }

    /**
       �������ե�����Υꥹ�Ȥ򹹿����ޤ���gcda�ե������ѡ���������
       �˸ƤӽФ�ɬ�פ�����ޤ���
    */
    private void updateSourceList() {
	Collection<FunctionGraph> all = getFunctionGraphCollection();
	for (FunctionGraph g : all) {
	    g.addLineCounts(sourceList);
	}
    }

    /**
       �Ρ��Ȥ�XML�����ǽ��Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	File file = getOrigin().getNoteFile();
	out.printf("<note file='%s' version='0x%x' stamp='0x%x'"
		   + " lastModified='%d'>\n",
		   XML.escape(file.getPath()), getVersion(), getStamp(),
		   file.lastModified());
	sourceList.printXML(out);
	Collection<FunctionGraph> all = getFunctionGraphCollection();
	for (FunctionGraph g : all) {
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
       @throws IOException �����ϥ��顼
    */
    public static Note parse(final String path) throws IOException {
	if (!path.endsWith(".gcno")) {
	    System.err.printf("%s: suffix is not '.gcno'.%n", path);
	    return null;
	}
	Note note = new Note(path);
	try {
	    note.parseNote();
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return null;
	} catch (FileNotFoundException e) {
	    System.err.printf("%s: not found.%n", path);
	    return null;
	}
	try {
	    note.parseData();
	} catch (CorruptedFileException e) {
	    e.printStackTrace();
	    return note;
	} catch (FileNotFoundException e) {
	    File dataFile = note.getOrigin().getDataFile();
	    System.err.printf("%s: not found.%n", dataFile.getPath());
	    return note;
	}
	note.updateSourceList();
	return note;
    }

    /** ���ꥸ�����Ӥ��륳��ѥ졼���Ǥ��� */
    private static Comparator<Note> originComparator;

    static {
	originComparator = new Comparator<Note>() {
	    public int compare(final Note n1,
			       final Note n2) {
		return n1.getOrigin().compareTo(n2.getOrigin());
	    }
	};
    }

    /**
       ���ꥸ�����Ӥ��륳��ѥ졼�����֤��ޤ���

       @return ���ꥸ�����Ӥ��륳��ѥ졼��
    */
    public static Comparator<Note> getOriginComparator() {
	// Comparable��������Ƥ⤤������...
	return originComparator;
    }
}
