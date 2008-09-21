package com.maroontress.coverture;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.TreeSet;

/**
   ���Х�å����륽�����ե������������ޤ���
*/
public final class Source {

    /** ���Х�å��оݤΥ������ե�����Υѥ��Ǥ��� */
    private String sourceFile;

    /** ���ֹ�ȹԾ���ΥޥåפǤ��� */
    private HashMap<Integer, LineInfo> map;

    /** �������ե�����˴ޤޤ��ؿ��δؿ�����դΥ��åȤǤ��� */
    private TreeSet<FunctionGraph> functions;

    /**
       ���������������ޤ���

       @param sourceFile �������ե�����Υѥ�
    */
    public Source(final String sourceFile) {
	this.sourceFile = sourceFile;
	map = new HashMap<Integer, LineInfo>();
	functions = new TreeSet<FunctionGraph>(
	    FunctionGraph.getLineNumberComparator());
    }

    /**
       ���Υ������˴ޤޤ��ؿ�����դ��ɲä��ޤ���

       @param fg �ؿ������
    */
    public void addFunctionGraph(final FunctionGraph fg) {
	functions.add(fg);
    }

    /**
       ���ֹ�μ¹Բ����û����ޤ���

       @param lineNuber ���ֹ�
       @param delta �¹Բ��
    */
    public void addLineCount(final int lineNuber, final long delta) {
	LineInfo info = map.get(lineNuber);
	if (info == null) {
	    info = new LineInfo();
	    map.put(lineNuber, info);
	}
	info.addCount(delta);
    }

    /**
       ���ֹ�μ¹Բ����������ޤ���

       @param lineNuber
       @return �¹Բ��
    */
    public long getLineCount(final int lineNuber) {
	LineInfo info = map.get(lineNuber);
	if (info == null) {
	    return -1;
	}
	return info.getCount();
    }

    /**
       ��Ψ����ѡ�����Ȥ�׻����ޤ���ʬ�줬0�ΤȤ���0���֤��ޤ���

       @param n ʬ��
       @param m ʬ��
       @return n/m�Υѡ������
    */
    private int percent(final long n, final long m) {
	if (m == 0) {
	    return 0;
	}
	return (int)(100.0 * n / m + 0.5);
    }

    /**
       gcov�ߴ��Υ��Х�å���̤���Ϥ��ޤ���

       @param out ������
       @param timestamp gcno�ե�����Υ����ॹ�����
    */
    private void outputLines(final PrintWriter out, final long timestamp)
	throws IOException {
	File file = new File(sourceFile);
	if (file.lastModified() > timestamp) {
	    System.err.printf("%s: source file is newer than gcno file\n",
			      sourceFile);
	    out.printf("%9s:%5d:Source is newer than gcno file\n", "-", 0);
	}
	LineNumberReader in = new LineNumberReader(new FileReader(file));
	String line;
	while ((line = in.readLine()) != null) {
	    int num = in.getLineNumber();
	    while (functions.size() > 0
		   && functions.first().getLineNumber() == num) {
		FunctionGraph fg = functions.pollFirst();
		long calledCount = fg.getCalledCount();
		long returnedCount = fg.getReturnedCount();
		int executedBlocks = fg.getExecutedBlockCount();
		int allBlocks = fg.getBlockCount();
		out.printf("function %s called %d returned %d%%"
			   + " blocks executed %d%%\n",
			   fg.getFunctionName(), calledCount,
			   percent(returnedCount, calledCount),
			   percent(executedBlocks, allBlocks));
	    }

	    LineInfo info = map.get(num);
	    long count;
	    String mark;
	    if (info == null) {
		mark = "-";
	    } else if ((count = info.getCount()) == 0) {
		mark = "#####";
	    } else {
		mark = String.valueOf(count);
	    }
	    out.printf("%9s:%5d:%s\n", mark, num, line);
	}
	in.close();
    }

    /**
    */
    public void outputFile(final String pathPrefix, final long timestamp)
	throws IOException {
	String path = pathPrefix + "#" + sourceFile;
	path = path.replaceAll("/", "#");
	File file = new File(path);
	PrintWriter out = new PrintWriter(file);
	try {
	    outputLines(out, timestamp);
	} finally {
	    out.close();
	}
    }
}
