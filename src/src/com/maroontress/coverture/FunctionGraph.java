package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractFunctionGraph;
import com.maroontress.gcovparser.CorruptedFileException;
import com.maroontress.gcovparser.gcno.FunctionGraphRecord;
import java.io.PrintWriter;
import java.util.Comparator;

/**
   �ؿ�����դǤ���
*/
public final class FunctionGraph extends AbstractFunctionGraph<Block, Arc> {

    /** {@inheritDoc} */
    @Override protected Block createBlock(final int id, final int blockFlags) {
	return new Block(id, blockFlags);
    }

    /** {@inheritDoc} */
    @Override protected Arc createArc(final Block start, final Block end,
				      final int flags) {
	return new Arc(start, end, flags);
    }

    /**
       �������ꥹ�Ȥˤ��δؿ�����դ��ɲä������٤ƤΥ֥�å��ι��ֹ�
       ��μ¹Բ����ޡ������ޤ���

       �ե�����դ���褷�Ƥ��ʤ����ϲ��⤷�ޤ���

       @param sourceList �������ꥹ��
    */
    public void addLineCounts(final SourceList sourceList) {
	if (!isSolved()) {
	    return;
	}
	Source source = sourceList.getSource(getSourceFile());
	source.addFunctionGraph(this);
	Iterable<Block> blocks = getBlocks();
	for (Block b : blocks) {
	    b.addLineCounts(sourceList);
	}
    }

    /**
       �ؿ�����դ�XML�����ǽ��Ϥ��ޤ���

       @param out ������
    */
    public void printXML(final PrintWriter out) {
	out.printf("<functionGraph id='%d' checksum='0x%x' functionName='%s'"
		   + " sourceFile='%s' lineNumber='%d'"
		   + " complexity='%d' complexityWithFake='%d'",
		   getId(), getChecksum(), XML.escape(getFunctionName()),
		   XML.escape(getSourceFile()), getLineNumber(),
		   getComplexity(), getComplexityWithFake());
	if (isSolved()) {
	    out.printf(" called='%d' returned='%d' executedBlocks='%d'",
		       getCalledCount(), getReturnedCount(),
		       getExecutedBlockCount());
	}
	out.printf(" allBlocks='%d'>\n", getBlockCount());
	Iterable<Block> blocks = getBlocks();
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
	super(rec);
    }

    /**
       �ؿ����Ϥޤ���ֹ����Ӥ��륳��ѥ졼���Ǥ���
    */
    private static Comparator<FunctionGraph> lineNumberComparator;

    static {
	lineNumberComparator = new Comparator<FunctionGraph>() {
	    public int compare(final FunctionGraph fg1,
			       final FunctionGraph fg2) {
		return fg1.getLineNumber() - fg2.getLineNumber();
	    }
	};
    }

    /**
       �ؿ����Ϥޤ���ֹ����Ӥ��륳��ѥ졼�����֤��ޤ���

       @return �ؿ����Ϥޤ���ֹ����Ӥ��륳��ѥ졼��
    */
    public static Comparator<FunctionGraph> getLineNumberComparator() {
	return lineNumberComparator;
    }
}
