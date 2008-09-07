package com.maroontress.coverture;

import com.maroontress.coverture.gcno.AnnounceFunctionRecord;
import com.maroontress.coverture.gcno.ArcRecord;
import com.maroontress.coverture.gcno.ArcsRecord;
import com.maroontress.coverture.gcno.FunctionGraphRecord;
import com.maroontress.coverture.gcno.LineRecord;
import com.maroontress.coverture.gcno.LinesRecord;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
   �ؿ�����դǤ���
*/
public final class FunctionGraph {

    /** ���̻ҤǤ��� */
    private int id;

    /** �ؿ��Υ����å�����Ǥ��� */
    private int checksum;

    /** �ؿ�̾�Ǥ��� */
    private String functionName;

    /** �����������ɤΥե�����̾�Ǥ��� */
    private String sourceFile;

    /** �ؿ����и����륽���������ɤιԿ��Ǥ��� */
    private int lineNumber;

    /** �ؿ�����������ܥ֥�å�������Ǥ��� */
    private Block[] blocks;

    /** ���٤ƤΥ��å��θĿ��Ǥ��� */
    private int totalArcCount;

    /**
       ���ѥ˥󥰥ĥ꡼�ǤϤʤ����å��θĿ��Ǥ������ѥ˥󥰥ĥ꡼�˥���
       ����1�Ĳä����٤ˡ���ϩ��1�����ä��ޤ������Τ��ᡢ�����ͤϥ���
       �դΥ��������󥯤����������㳰��longjmp()���θ��������
       McCabe�Υ�������ޥƥ��å�ʣ���٤��б����ޤ���
    */
    private int arcCount;

    /**
       ���ѥ˥󥰥ĥ꡼�ǤϤʤ����å��Τ��������Υ��å��θĿ��Ǥ�����
       �Υ��å��ϡ��㳰��longjmp()�ˤ�äơ����ߤδؿ�����ȴ������䡢
       exit()�ʤɤΤ褦�����ʤ��ؿ��θƤӽФ��η�ϩ���б����ޤ���
       arcCount���餳���ͤ������McCabe�Υ�������ޥƥ��å�ʣ���٤�ɽ
       ���ޤ���
    */
    private int fakeArcCount;

    /**
       �ؿ�����դ�XML�����ǽ��Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	if (false) {
	    int complexityWithFake = totalArcCount - blocks.length + 2;
	    int complexity = complexityWithFake - fakeArcCount;
	    // complexityWithFake == arcCount
	}
	out.printf("<functionGraph id='%d' checksum='0x%x' functionName='%s' "
		   + "sourceFile='%s' lineNumber='%d' "
		   + "complexity='%d' complexityWithFake='%d'>\n",
		   id, checksum, XML.escape(functionName),
		   XML.escape(sourceFile), lineNumber,
		   arcCount - fakeArcCount, arcCount);
	for (Block b : blocks) {
	    b.printXML(out);
	}
	out.printf("</functionGraph>\n");
    }

    /**
       �ؿ�����ե쥳���ɤ��饤�󥹥��󥹤��������ޤ���

       @param rec �ؿ�����ե쥳����
       @throws CorruptedFileException
    */
    public FunctionGraph(final FunctionGraphRecord rec)
	throws CorruptedFileException {
	AnnounceFunctionRecord announce = rec.getAnnounce();
	id = announce.getId();
	checksum = announce.getChecksum();
	functionName = announce.getFunctionName();
	sourceFile = announce.getSourceFile();
	lineNumber = announce.getLineNumber();

	int[] blockFlags = rec.getBlocks().getFlags();
	blocks = new Block[blockFlags.length];
	for (int k = 0; k < blockFlags.length; ++k) {
	    blocks[k] = new Block(k, blockFlags[k]);
	}

	ArcsRecord[] arcs = rec.getArcs();
	for (ArcsRecord e : arcs) {
	    addArcsRecord(e);
	}

	LinesRecord[] lines = rec.getLines();
	for (LinesRecord e : lines) {
	    addLinesRecord(e);
	}
    }

    /**
       ARCS�쥳���ɤ��饨�å����������ơ��ؿ�����դ��ɲä��ޤ���

       @param arcsRecord ARCS�쥳����
    */
    private void addArcsRecord(final ArcsRecord arcsRecord)
	throws CorruptedFileException {
	int startIndex = arcsRecord.getStartIndex();
	ArcRecord[] list = arcsRecord.getList();
	if (startIndex >= blocks.length) {
	    throw new CorruptedFileException();
	}
	for (ArcRecord arcRecord : list) {
	    int endIndex = arcRecord.getEndIndex();
	    int flags = arcRecord.getFlags();
	    if (endIndex >= blocks.length) {
		throw new CorruptedFileException();
	    }
	    Block start = blocks[startIndex];
	    Block end = blocks[endIndex];
	    Arc arc = new Arc(start, end, flags);
	    if (!arc.isOnTree()) {
		++arcCount;
	    }
	    if (arc.isFake()) {
		++fakeArcCount;
	    }
	}
	totalArcCount += list.length;
    }

    /**
       LINES�쥳���ɤ���ԥ���ȥ�Υꥹ�Ȥ��������ơ��ؿ�����դ��ɲ�
       ���ޤ���

       @param linesRecord LINES�쥳����
    */
    private void addLinesRecord(final LinesRecord linesRecord)
	throws CorruptedFileException {
	int blockIndex = linesRecord.getBlockIndex();
	LineRecord[] list = linesRecord.getList();
	if (blockIndex >= blocks.length) {
	    throw new CorruptedFileException();
	}
	LineEntryList entryList = new LineEntryList(sourceFile);
	for (LineRecord rec : list) {
	    int number = rec.getNumber();
	    if (number == 0) {
		entryList.changeFileName(rec.getFileName());
	    } else {
		entryList.addLineNumber(number);
	    }
	}
	blocks[blockIndex].setLines(entryList.getLineEntries());
    }

    /**
       ���̻Ҥ�������ޤ���

       @return ���̻�
    */
    public int getId() {
	return id;
    }
}
