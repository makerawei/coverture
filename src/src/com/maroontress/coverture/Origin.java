package com.maroontress.coverture;

import java.io.File;

/**
   gcno�ե�����˴ط�����ѥ���������ޤ���
*/
public final class Origin {

    /** gcno�ե�����Ǥ��� */
    private File gcnoFile;

    /** �б�����gcda�ե�����Ǥ��� */
    private File gcdaFile;

    /**
       basePath�򥨥������פ���ʸ����Ǥ���
    */
    private String pathPrefix;

    /**
       ���ꥸ����������ޤ���

       path�γ�ĥ�Ҥ�.gcno�Ǥʤ���Фʤ�ޤ���

       @param path gcno�ե�����Υѥ�
       @throws IllegalArgumentException �ѥ���.gcno�ǽ����ʤ����
    */
    public Origin(final String path) {
	if (!path.endsWith(".gcno")) {
	    String m = String.format("%s: suffix is not '.gcno'.", path);
	    throw new IllegalArgumentException(m);
	}
	gcnoFile = new File(path);
	String basePath = path.substring(0, path.lastIndexOf('.'));
	gcdaFile = new File(basePath + ".gcda");
	pathPrefix = escapeGcov(basePath);
    }

    /**
       �ѥ���gcov�Υ�������ǥ��������פ��ޤ���

       @param path �ѥ�
       @return ���������פ����ѥ�
    */
    private String escapeGcov(final String path) {
	String s = path.replace(File.separatorChar, '#');
	return s.replaceAll("#\\.#", "#").replaceAll("#\\.\\.#", "#^#");
    }

    /**
       gcno�ե������������ޤ���

       @return gcno�ե�����
    */
    public File getNoteFile() {
	return gcnoFile;
    }

    /**
       gcda�ե������������ޤ���

       @return gcda�ե�����
    */
    public File getDataFile() {
	return gcdaFile;
    }

    /**
       ���Х�å��ե�����Υѥ���������ޤ���

       �ѥ��ϡ�gcno�ե�����Υѥ������ĥ�Ҥ����������Τˡ����Х��
       ���оݤΥ������ե�����Υѥ��� "##" ��Ϣ�뤷���塢���Υ롼���
       �Ѵ�����ʸ����ˤʤ�ޤ���

       - �ѥ�����ݡ��ͥ�Ȥζ��ڤ�ʸ�����#�פ��Ѵ�
       - �ѥ�����ݡ��ͥ�Ȥ���.�פξ��Ϻ��
       - �ѥ�����ݡ��ͥ�Ȥ���..�פξ��ϡ�^�פ��Ѵ�

       @param sourceFile ���Х�å��оݤΥ������ե�����Υѥ�
       @return ���Х�å��ե�����Υѥ�
    */
    public String getCoverageFilePath(final String sourceFile) {
	return pathPrefix + "##" + escapeGcov(sourceFile) + ".gcov";
    }
}
