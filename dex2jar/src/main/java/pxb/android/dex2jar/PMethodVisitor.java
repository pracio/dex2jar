/**
 * 
 */
package pxb.android.dex2jar;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * @author Panxiaobo [pxb1988@126.com]
 * 
 */
public class PMethodVisitor extends MethodAdapter implements Opcodes {

	private Map<Label, Type> handlers = new HashMap<Label, Type>();
	private Type[] local = new Type[100];
	private Stack<Type> stack = new Stack<Type>();

	public PMethodVisitor(MethodVisitor mv) {
		super(mv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.asm.MethodAdapter#visitTryCatchBlock(org.objectweb.asm.
	 * Label, org.objectweb.asm.Label, org.objectweb.asm.Label,
	 * java.lang.String)
	 */
	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		super.visitTryCatchBlock(start, end, handler, type);
		this.handlers.put(handler, Type.getObjectType(type));
	}

	public void visit(Type owner, String des, boolean isStatic) {
		Type args[] = Type.getArgumentTypes(des);
		if (isStatic) {
			for (int i = 0; i < args.length; i++) {
				local[i] = (args[i]);
			}
		} else {
			local[0] = owner;
			for (int i = 1; i <= args.length; i++) {
				local[i] = (args[i - 1]);
			}
		}
	}

	private void e(Type stack, Type need) {
		if (stack == null || !stack.equals(need)) {
			System.out.println("Type: " + stack + " != " + need);
		}
	}

	public Type getFromLocal(int i) {
		return local[i];
	}

	/**
	 *1开始
	 * 
	 * @param i
	 * @return
	 */
	public Type getFromStack(int i) {
		return stack.get(stack.size() - i);
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		super.visitFieldInsn(opcode, owner, name, desc);
		switch (opcode) {
		case Opcodes.GETFIELD:
			e((Type) stack.pop(), Type.getObjectType(owner));
			stack.push(Type.getType(desc));
			break;
		case Opcodes.PUTFIELD:
			e((Type) stack.pop(), Type.getType(desc));
			e((Type) stack.pop(), Type.getObjectType(owner));
			break;
		case Opcodes.GETSTATIC:
			stack.push(Type.getType(desc));
			break;
		case Opcodes.PUTSTATIC:
			e((Type) stack.pop(), Type.getType(desc));
			break;
		}
	}

	public void visitInsn(int opcode) {
		super.visitInsn(opcode);
		switch (opcode) {
		case ACONST_NULL:
			stack.push(Type.VOID_TYPE);
			break;
		case ICONST_M1:
		case ICONST_0:
		case ICONST_1:
		case ICONST_2:
		case ICONST_3:
		case ICONST_4:
		case ICONST_5:
		case LCONST_0:
			stack.push(Type.INT_TYPE);
			break;
		case LCONST_1:
			stack.push(Type.LONG_TYPE);
			break;
		case FCONST_0:
		case FCONST_1:
		case FCONST_2:
			stack.push(Type.FLOAT_TYPE);
			break;
		case DCONST_0:
		case DCONST_1:
			stack.push(Type.DOUBLE_TYPE);
			break;
		case IALOAD:
		case LALOAD:
		case FALOAD:
		case DALOAD:
		case BALOAD:
		case CALOAD:
		case AALOAD:
		case SALOAD:
			stack.pop();
			Type base = (Type) stack.pop();
			stack.push(base.getElementType());
			break;
		case IASTORE:
		case LASTORE:
		case FASTORE:
		case DASTORE:
		case AASTORE:
		case BASTORE:
		case CASTORE:
		case SASTORE:

			stack.pop();
			stack.pop();
			stack.pop();
			break;
		case POP:
			stack.pop();
			break;
		case DUP:
			stack.push(stack.peek());
			break;
		case IRETURN:
		case LRETURN:
		case FRETURN:
		case DRETURN:
		case ARETURN:
		case ATHROW:
			stack.pop();
			break;
		case ARRAYLENGTH:
			stack.pop();
			stack.push(Type.INT_TYPE);
			break;
		case IADD:
		case LADD:
		case FADD:
		case DADD:
		case ISUB:
		case LSUB:
		case FSUB:
		case DSUB:
		case IMUL:
		case LMUL:
		case FMUL:
		case DMUL:
		case IDIV:
		case LDIV:
		case FDIV:
		case DDIV:
		case IREM:
		case LREM:
		case FREM:
		case DREM:
		case INEG:
		case LNEG:
		case FNEG:
		case DNEG:
		case ISHL:
		case LSHL:
		case ISHR:
		case LSHR:
		case IUSHR:
		case LUSHR:
		case IAND:
		case LAND:
		case IOR:
		case LOR:
		case IXOR:
		case LXOR:
		case MONITORENTER:
		case MONITOREXIT:
		case LCMP:
		case FCMPL:
		case FCMPG:
		case DCMPL:
		case DCMPG:
			stack.pop();
			break;

		case DUP_X1:
			Type a = stack.pop();
			Type b = stack.pop();
			stack.push(a);
			stack.push(b);
			stack.push(a);
			break;

		case I2L:
		case I2F:
		case I2D:
		case L2I:
		case L2F:
		case L2D:
		case F2I:
		case F2L:
		case F2D:
		case D2I:
		case D2L:
		case D2F:
		case I2B:
		case I2C:
		case I2S:

		case RETURN:

			break;
		case POP2:
		case DUP_X2:
		case DUP2:
		case DUP2_X1:
		case DUP2_X2:
		case SWAP:
		default:
			throw new RuntimeException("");
		}
	}

	public void visitIntInsn(int opcode, int operand) {
		super.visitIntInsn(opcode, operand);
		switch (opcode) {
		case BIPUSH:
			stack.push(Type.BYTE_TYPE);
			break;
		case SIPUSH:
			stack.push(Type.SHORT_TYPE);
			break;
		case NEWARRAY:
			stack.pop();
			switch (operand) {
			case Opcodes.T_BOOLEAN:
				stack.push(Type.getType("[Z"));
				break;
			case Opcodes.T_BYTE:
				stack.push(Type.getType("[B"));
				break;
			case Opcodes.T_CHAR:
				stack.push(Type.getType("[C"));
				break;
			case Opcodes.T_DOUBLE:
				stack.push(Type.getType("[D"));
				break;
			case Opcodes.T_FLOAT:
				stack.push(Type.getType("[F"));
				break;
			case Opcodes.T_INT:
				stack.push(Type.getType("[I"));
				break;
			case Opcodes.T_LONG:
				stack.push(Type.getType("[J"));
				break;
			case Opcodes.T_SHORT:
				stack.push(Type.getType("[Z"));
				break;
			}
			break;
		}
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		super.visitJumpInsn(opcode, label);
		switch (opcode) {
		case IFEQ:
		case IFNE:
		case IFLT:
		case IFGE:
		case IFGT:
		case IFLE:
		case IFNONNULL:
		case IFNULL:
			stack.pop();
			break;
		case IF_ICMPEQ:
		case IF_ICMPNE:
		case IF_ICMPLT:
		case IF_ICMPGE:
		case IF_ICMPGT:
		case IF_ICMPLE:
		case IF_ACMPEQ:
		case IF_ACMPNE:
			stack.pop();
			stack.pop();
			break;
		case GOTO:
		case JSR:
		}
	}

	@Override
	public void visitLabel(Label label) {
		super.visitLabel(label);
		Type type = handlers.get(label);
		if (type != null) {
			stack.push(type);
		}
	}

	public void visitLdcInsn(Object cst) {
		if (cst instanceof String) {
			stack.push(Type.getType(String.class));
		} else if (cst instanceof Integer) {
			stack.push(Type.INT_TYPE);
		} else if (cst instanceof Float) {
			stack.push(Type.FLOAT_TYPE);
		} else if (cst instanceof Long) {
			stack.push(Type.LONG_TYPE);
		} else if (cst instanceof Double) {
			stack.push(Type.DOUBLE_TYPE);
		} else if (cst instanceof Type) {
			stack.push(Type.getType(Class.class));
		}
		super.visitLdcInsn(cst);
	}

	@Override
	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		super.visitLookupSwitchInsn(dflt, keys, labels);
		stack.pop();
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {

		Type args[] = Type.getArgumentTypes(desc);
		for (int i = args.length - 1; i >= 0; i--) {
			Type t = (Type) stack.pop();
			e(t, args[i]);
		}
		if (opcode != Opcodes.INVOKESTATIC) {
			Type o = Type.getObjectType(owner);
			Type p = (Type) stack.pop();
			e(o, p);
		}

		Type ret = Type.getReturnType(desc);
		if (!ret.equals(Type.VOID_TYPE)) {
			stack.push(ret);
		}
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		super.visitMultiANewArrayInsn(desc, dims);
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		super.visitTableSwitchInsn(min, max, dflt, labels);
		stack.pop();
	}

	public void visitTypeInsn(int opcode, String type) {
		super.visitTypeInsn(opcode, type);
		switch (opcode) {
		case NEW:
			stack.push(Type.getObjectType(type));
			break;
		case CHECKCAST:
			stack.pop();
			stack.push(Type.getObjectType(type));
			break;
		case INSTANCEOF:
			stack.pop();
			stack.push(Type.BOOLEAN_TYPE);
			break;
		case ANEWARRAY:
			stack.pop();
			stack.push(Type.getObjectType("[L" + type + ";"));
			break;
		}
	}

	public void visitVarInsn(int opcode, int var) {
		super.visitVarInsn(opcode, var);
		switch (opcode) {
		case ILOAD:
		case LLOAD:
		case FLOAD:
		case DLOAD:
		case ALOAD:
			stack.push(local[var]);
			break;
		case ISTORE:
		case LSTORE:
		case FSTORE:
		case DSTORE:
		case ASTORE:
			Type p = stack.pop();
			// if (local[var] == null)
			local[var] = p;
			break;
		case RET:
		}
	}
}
