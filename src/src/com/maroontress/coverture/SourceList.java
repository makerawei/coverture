package com.maroontress.coverture;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

/**
   �ҤȤĤ�gcno�ե����뤬���Ȥ��륽�����ե�����Υꥹ�ȤǤ���
*/
public final class SourceList {

    /** �������ե�����Υѥ��ȥ������ΥޥåפǤ��� */
    private HashMap<String, Source> map;

    /**
       �������ꥹ�Ȥ��������ޤ���
    */
    public SourceList() {
	map = new HashMap<String, Source>();
    }

    /**
       �������ե�����Υѥ����б����륽������������ޤ���

       @param sourceFile �������ե�����Υѥ�
       @return ������
    */
    public Source getSource(final String sourceFile) {
	Source source = map.get(sourceFile);
	if (source == null) {
	    source = new Source(sourceFile);
	    map.put(sourceFile, source);
	}
	return source;
    }

    /**
       ���٤ƤΥ��Х�å��ե�������������ޤ���

       @param origin gcno�ե�����Υ��ꥸ��
       @param runs �ץ����μ¹Բ��
       @param programs �ץ����ο�
    */
    public void outputFiles(final Origin origin, final int runs,
			    final int programs) {
	Collection<Source> all = map.values();
	for (Source s : all) {
	    try {
		s.outputFile(origin, runs, programs);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    /**
       �������ե�����Υꥹ�ȤΥ��ޥ��XML�����ǽ��Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	out.printf("<sourceList>\n");
	Collection<Source> all = map.values();
	for (Source s : all) {
	    String path = s.getPath();
	    int executedLines = s.getExecutedLines();
	    int executableLines = s.getExecutableLines();
	    out.printf("<source file='%s' executableLines='%d'",
		       path, executableLines);
	    if (executableLines > 0) {
		out.printf(" executedLines='%d' rate='%.2f'",
			   executedLines,
			   100.0 * executedLines / executableLines);
	    }
	    out.printf("/>\n");
	}
	out.printf("</sourceList>\n");
    }
}
