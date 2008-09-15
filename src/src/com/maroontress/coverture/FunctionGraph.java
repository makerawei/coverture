package com.maroontress.coverture;

import com.maroontress.coverture.gcda.FunctionDataRecord;
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
import java.util.LinkedList;

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

    /** ���٤ƤΥ������θĿ��Ǥ��� */
    private int totalArcCount;

    /**
       ���Υ������θĿ��Ǥ������Υ������ϡ��㳰��longjmp()�ˤ�äơ���
       �ߤδؿ�����ȴ�����ϩ���б����ޤ���
    */
    private int fakeArcCount;

    /** �¹Բ����Ƚ�����Ƥ��륢�����Υꥹ�ȤǤ��� */
    private ArrayList<Arc> solvedArcs;

    /** �¹Բ���������ʥ������Υꥹ�ȤǤ��� */
    private ArrayList<Arc> unsolvedArcs;

    /**
       �ؿ�����դ�XML�����ǽ��Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	int complexityWithFake = totalArcCount - blocks.length + 2;
	int complexity = complexityWithFake - fakeArcCount;
	out.printf("<functionGraph id='%d' checksum='0x%x' functionName='%s' "
		   + "sourceFile='%s' lineNumber='%d' "
		   + "complexity='%d' complexityWithFake='%d'>\n",
		   id, checksum, XML.escape(functionName),
		   XML.escape(sourceFile), lineNumber,
		   complexity, complexityWithFake);
	for (Block b : blocks) {
	    b.printXML(out);
	}
	out.printf("</functionGraph>\n");
    }

    /**
       �ؿ�����ե쥳���ɤ��饤�󥹥��󥹤��������ޤ���

       @param rec �ؿ�����ե쥳����
       @throws CorruptedFileException �ե�����ι�¤������Ƥ��뤳�Ȥ򸡽�
    */
    public FunctionGraph(final FunctionGraphRecord rec)
	throws CorruptedFileException {
	AnnounceFunctionRecord announce = rec.getAnnounce();
	id = announce.getId();
	checksum = announce.getChecksum();
	functionName = announce.getFunctionName();
	sourceFile = announce.getSourceFile();
	lineNumber = announce.getLineNumber();
	solvedArcs = new ArrayList<Arc>();
	unsolvedArcs = new ArrayList<Arc>();

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

	if (blocks.length < 2) {
	    throw new CorruptedFileException("lacks entry and/or exit blocks");
	}
	Block entryBlock = blocks[0];
	Block exitBlock = blocks[blocks.length - 1];
	if (entryBlock.getInArcs().size() != 0) {
	    throw new CorruptedFileException("has arcs to entry block");
	}
	if (exitBlock.getOutArcs().size() != 0) {
	    throw new CorruptedFileException("has arcs from exit block");
	}
	for (Block e : blocks) {
	    e.presolve();
	}
    }

    /**
       ARCS�쥳���ɤ��饢�������������ơ��ؿ�����դ��ɲä��ޤ���

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
		/*
		  ���ѥ˥󥰥ĥ꡼�ǤϤʤ���������gcda�ե�����ˤϤ���
		  ���������б�����¹Բ������Ͽ����롣�������μ¹Բ�
		  �����ºݤ˲�褹��Τ�setFunctionDataRecord()�᥽��
		  �ɤ��ƤФ줿�Ȥ���
		*/
		solvedArcs.add(arc);
	    } else {
		/*
		  ���ѥ˥󥰥ĥ꡼�Υ��������������μ¹Բ����gcda�ե�
		  ���뤫������Ǥ��ʤ��Τǡ��ե�����դ�򤯤��Ȥǥ���
		  ���μ¹Բ������ʤ���Фʤ�ʤ���
		*/
		unsolvedArcs.add(arc);
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
       �ؿ��ǡ����쥳���ɤ�ؿ�����դ��ɲä��ޤ���

       @param rec �ؿ��ǡ����쥳����
       @throws CorruptedFileException
    */
    public void setFunctionDataRecord(final FunctionDataRecord rec)
	throws CorruptedFileException {
	if (checksum != rec.getChecksum()) {
	    String m = String.format("gcda file: checksum mismatch for '%s'",
				     functionName);
	    throw new CorruptedFileException(m);
	}
	long[] arcCounts = rec.getArcCounts();
	if (solvedArcs.size() != arcCounts.length) {
	    String m = String.format("gcda file: profile mismatch for '%s'",
				     functionName);
	    throw new CorruptedFileException(m);
	}
	for (int k = 0; k < arcCounts.length; ++k) {
	    solvedArcs.get(k).addCount(arcCounts[k]);
	}
	solveFlowGraph();
    }

    /**
       �ե�����դ�򤭤ޤ���

       @throws CorruptedFileException
    */
    private void solveFlowGraph() throws CorruptedFileException {
	LinkedList<Block> invalidBlocks = new LinkedList<Block>();
	for (Block e : blocks) {
	    e.sortOutArcs();
	    invalidBlocks.add(e);
	}

	LinkedList<Block> validBlocks = new LinkedList<Block>();
	Block e;
	while ((e = invalidBlocks.poll()) != null) {
	    e.validate(validBlocks);
	}
	while ((e = validBlocks.poll()) != null) {
	    e.validateSides(validBlocks);
	}
	for (Block b : blocks) {
	    if (!b.getCountValid()) {
		throw new CorruptedFileException("graph is unsolvable");
	    }
	}
    }

    /**
       ���̻Ҥ�������ޤ���

       @return ���̻�
    */
    public int getId() {
	return id;
    }
}
