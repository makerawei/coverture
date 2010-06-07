package com.maroontress.coverture;

import com.maroontress.gcovparser.AbstractArc;

/**
   �ؿ�����դΥ��å��Ȥʤ륢�����μ������饹�Ǥ���
*/
public final class Arc extends AbstractArc<Block, Arc> {

    /**
       ���������������ޤ��������������󥹥��󥹤ϳ��ϥ֥�å��ΡֽФ�
       �������ס���λ�֥�å��Ρ����륢�����פ��ɲä���ޤ���

       @param start ���ϥ֥�å�
       @param end ��λ�֥�å�
       @param flags �ե饰
    */
    public Arc(final Block start, final Block end, final int flags) {
	super(start, end, flags);
    }

    /** {@inheritDoc} */
    @Override protected Arc cast() {
	return this;
    }
}
