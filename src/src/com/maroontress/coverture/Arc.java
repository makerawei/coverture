package com.maroontress.coverture;

/**
   �ؿ�����դΥ��å��Ǥ������å��ˤϸ��������ꡢ���ϥ֥�å�����Ф�
   ��λ�֥�å�������ޤ���
*/
public final class Arc {

    /**
       ���ѥ˥󥰥ĥ꡼�������륨�å���ɽ���ե饰�Ǥ���
    */
    private static final int FLAG_ON_TREE = 0x1;

    /**
       ���Υ��å���ɽ���ե饰�Ǥ������Υ��å��ϡ��㳰��longjmp()�ˤ��
       �ơ����ߤδؿ�����ȴ������䡢exit()�ʤɤΤ褦�����ʤ��ؿ�
       �θƤӽФ��η�ϩ��ɽ���ޤ���
    */
    private static final int FLAG_FAKE = 0x2;

    /**
       ���å���ʬ�����ʤ��ä���ϩ�Ǥ��뤳�Ȥ�ɽ���ե饰�Ǥ���
    */
    private static final int FLAG_FALL_THROUGH = 0x4;

    /** ���ϥ֥�å��Ǥ��� */
    private Block start;

    /** ��λ�֥�å��Ǥ��� */
    private Block end;

    /**
       ���å��Υե饰�Ǥ���FLAG_ON_TREE, FLAG_FAKE, FLAG_FALL_THROUGH
       �������¤ˤʤ�ޤ���
    */
    private int flags;

    /**
       Arc is for a function that abnormally returns: ���Υ��å���
       �ؿ��θƤӽФ��������ʤ����Ȥ򼨤��ޤ���
    */
    private boolean callNonReturn;

    /**
       Arc is for catch/setjmp: ���Υ��å��ιԤ��褬catch�ޤ���
       setjmp()�Ǥ��뤳�Ȥ򼨤��ޤ���
    */
    private boolean nonLocalReturn;

    /** ̤���� */
    private int count;

    /** ̤���� */
    private boolean countValid;

    /**
       ���å����������ޤ��������������󥹥��󥹤ϳ��ϥ֥�å��ΡֽФ�
       ���å��ס���λ�֥�å��Ρ����륨�å��פ��ɲä���ޤ���

       @param start ���ϥ֥�å�
       @param end ��λ�֥�å�
       @param flags �ե饰
    */
    public Arc(final Block start, final Block end, final int flags) {
	this.start = start;
	this.end = end;
	this.flags = flags;
	start.addOutArc(this);
	end.addInArc(this);
	if (isFake()) {
	    if (start.getId() != 0) {
		/*
		  Exceptional exit from this function, the source
		  block must be a call.
		*/
		start.setCallSite(true);
		callNonReturn = true;
	    } else {
		/*
		  Non-local return from a callee of this function. The
		  destination block is a catch or setjmp.
		*/
		end.setNonLocalReturn(true);
		nonLocalReturn = true;
	    }
	}
    }

    /**
       ���å������ѥ˥󥰥ĥ꡼�������뤫�ɤ����������ޤ���

       @return ���ѥ˥󥰥ĥ꡼�����������true�������Ǥʤ����
       false
    */
    public boolean isOnTree() {
	return (flags & FLAG_ON_TREE) != 0;
    }

    /**
       ���å������Υ��å����ɤ����������ޤ���

       @return ���Υ��å��ξ���true�������Ǥʤ����false
    */
    public boolean isFake() {
	return (flags & FLAG_FAKE) != 0;
    }

    /**
       ���å���ʬ�����ʤ��ä���ϩ�Ǥ��뤫�ɤ����������ޤ���

       @return ���å���ʬ�����ʤ��ä���ϩ�ξ���true�������Ǥʤ����
       false
    */
    public boolean isFallThrough() {
	return (flags & FLAG_FALL_THROUGH) != 0;
    }

    /**
       ���å���exit()�ʤɤΤ褦�����ʤ��ؿ��θƤӽФ��Ǥ��뤫�ɤ���
       �������ޤ���

       @return ���ʤ��ؿ��θƤӽФ��ξ���true�������Ǥʤ����false
    */
    public boolean isCallNonReturn() {
	return callNonReturn;
    }

    /**
       ���å��ιԤ��褬catch�ޤ���setjmp()�Ǥ��뤫�ɤ�����������ޤ���

       @return ���å��ιԤ��褬catch�ޤ���setjmp()�Ǥ������true����
       ���Ǥʤ����false
    */
    public boolean isNonLocalReturn() {
	return nonLocalReturn;
    }

    /**
       ���å��ν�λ�֥�å���������ޤ���

       @return ��λ�֥�å�
    */
    public Block getEnd() {
	return end;
    }
}
