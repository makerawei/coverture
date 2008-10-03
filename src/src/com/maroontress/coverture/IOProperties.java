package com.maroontress.coverture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
   �����Ϥ˴�Ϣ����ץ�ѥƥ��Ǥ���
*/
public final class IOProperties {

    /** �ե��������Ϥ���ǥ��쥯�ȥ�Ǥ��� */
    private File outputDir;

    /** �������ե������ʸ������Ǥ��� */
    private Charset sourceFileCharset;

    /** gcov�ե������ʸ������Ǥ��� */
    private Charset gcovFileCharset;

    /**
       �ǥե���Ȥ������ϥץ�ѥƥ����������ޤ���
    */
    public IOProperties() {
	outputDir = new File(".");
	sourceFileCharset = Charset.defaultCharset();
	gcovFileCharset = Charset.defaultCharset();
    }

    /**
       @param dir �ե��������Ϥ���ǥ��쥯�ȥ�
    */
    public void setOutputDir(final File dir) {
	outputDir = dir;
    }

    /**
       @param cs �������ե������ʸ������
    */
    public void setSourceFileCharset(final Charset cs) {
	sourceFileCharset = cs;
    }

    /**
       @param cs gcov�ե������ʸ������
    */
    public void setGcovFileCharset(final Charset cs) {
	gcovFileCharset = cs;
    }

    /**
       ���ϥե�������������ޤ���

       @param path ���ϥǥ��쥯�ȥ������Ȥ������Хѥ�
       @return ���ϥե�����
    */
    private File createOuputFile(final String path) {
	return new File(outputDir, path);
    }

    /**
       ������Υǥ��쥯�ȥ���������ޤ���
    */
    public void makeOutputDir() {
	outputDir.mkdirs();
    }

    /**
       gcov�ե�����Υ饤�����������ޤ���

       @param path ���ϥǥ��쥯�ȥ������Ȥ������Хѥ�
       @return gcov�ե�����Υ饤��
       @throws FileNotFoundException �ե�����������Ǥ��ʤ�
    */
    public Writer createGcovWriter(final String path)
	throws FileNotFoundException {
	File file = createOuputFile(path);
	Writer out = new OutputStreamWriter(new FileOutputStream(file),
					    gcovFileCharset);
	return out;
    }

    /**
       �������ե�����Υ꡼�����������ޤ���

       @param file �������ե�����
       @return �������ե�����Υ꡼��
       @throws FileNotFoundException �ե����뤬¸�ߤ��ʤ�
    */
    public Reader createSourceFileReader(final File file)
	throws FileNotFoundException {
	Reader in = new InputStreamReader(new FileInputStream(file),
					  sourceFileCharset);
	return in;
    }
}
