/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIExpressionManager;
import org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager;
import org.eclipse.cdt.debug.core.cdi.ICDIVariableManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIArrayType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIBoolType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDICharType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIEnumType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFloatType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIFunctionType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIIntType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongLongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDILongType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIPointerType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIReferenceType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIShortType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIStructType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIWCharType;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.Format;
import org.eclipse.cdt.debug.mi.core.cdi.MI2CDIException;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ArrayValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.BoolValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.CharValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.DoubleValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.EnumValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FloatValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.FunctionValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.IntValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongLongValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.LongValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.PointerValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ReferenceValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.ShortValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.StructValue;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.Type;
import org.eclipse.cdt.debug.mi.core.cdi.model.type.WCharValue;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIVarAssign;
import org.eclipse.cdt.debug.mi.core.command.MIVarListChildren;
import org.eclipse.cdt.debug.mi.core.command.MIVarSetFormat;
import org.eclipse.cdt.debug.mi.core.command.MIVarShowAttributes;
import org.eclipse.cdt.debug.mi.core.event.MIVarChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVar;
import org.eclipse.cdt.debug.mi.core.output.MIVarListChildrenInfo;
import org.eclipse.cdt.debug.mi.core.output.MIVarShowAttributesInfo;

/**
 */
public class Variable extends VariableObject implements ICDIVariable {

	MIVar miVar;
	Value value;
	VariableObject varObj;
	ICDIVariable[] children = new ICDIVariable[0];
	Type type;
	String editable = null;

	public Variable(VariableObject obj, MIVar v) {
		super(obj, obj.getName());
		miVar = v;
		varObj = obj;
	}

	public MIVar getMIVar() {
		return miVar;
	}

	public Variable getChild(String name) {
		for (int i = 0; i < children.length; i++) {
			Variable variable = (Variable)children[i];
			if (name.equals(variable.getMIVar().getVarName())) {
				return variable;
			} else {
				// Look also in the grandchildren.
				Variable grandChild = variable.getChild(name);
				if (grandChild != null) {
					return grandChild;
				}
			}
		}
		return null;
	}

	public ICDIVariable[] getChildren() throws CDIException {
		// Use the default timeout.
		return getChildren(-1);
	}

	/**
	 * This can be a potentially long operation for GDB.
	 * allow the override of the timeout.
	 */
	public ICDIVariable[] getChildren(int timeout) throws CDIException {
		Session session = (Session)(getTarget().getSession());
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarListChildren var =
			factory.createMIVarListChildren(getMIVar().getVarName());
		try {
			if (timeout >= 0) {
				mi.postCommand(var, timeout);
			} else {
				mi.postCommand(var);
			}
			MIVarListChildrenInfo info = var.getMIVarListChildrenInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MIVar[] vars = info.getMIVars();
			children = new Variable[vars.length];
			for (int i = 0; i < vars.length; i++) {
				VariableObject varObj = new VariableObject(getTarget(),
				 vars[i].getExp(), getStackFrame(),
				 getPosition(),getStackDepth());
				children[i] = new Variable(varObj, vars[i]);
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return children;
	}

	public int getChildrenNumber() throws CDIException {
		return miVar.getNumChild();
	}

	/**
	 * We overload the VariableObject since the gdb-varobject already knows
	 * the type and its probably more accurate.
	 * 
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		// We overload here not to use the whatis command.
		return miVar.getType();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getValue()
	 */
	public ICDIValue getValue() throws CDIException {
		if (value == null) {
			ICDIType t = getType();
			if (t instanceof ICDIBoolType) {
				value = new BoolValue(this);
			} else if (t instanceof ICDICharType) {
				value = new CharValue(this);
			} else if (t instanceof ICDIWCharType) {
				value = new WCharValue(this);
			} else if (t instanceof ICDIShortType) {
				value = new ShortValue(this);
			} else if (t instanceof ICDIIntType) {
				value = new IntValue(this);
			} else if (t instanceof ICDILongType) {
				value = new LongValue(this);
			} else if (t instanceof ICDILongLongType) {
				value = new LongLongValue(this);
			} else if (t instanceof ICDIEnumType) {
				value = new EnumValue(this);
			} else if (t instanceof ICDIFloatType) {
				value = new FloatValue(this);
			} else if (t instanceof ICDIDoubleType) {
				value = new DoubleValue(this);
			} else if (t instanceof ICDIFunctionType) {
				value = new FunctionValue(this);
			} else if (t instanceof ICDIPointerType) {
				value = new PointerValue(this);
			} else if (t instanceof ICDIReferenceType) {
				value = new ReferenceValue(this);
			} else if (t instanceof ICDIArrayType) {
				value = new ArrayValue(this);
			} else if (t instanceof ICDIStructType) {
				value = new StructValue(this);	
			} else {
				value = new Value(this);
			}
		}
		return value;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(ICDIValue)
	 */
	public void setValue(ICDIValue value) throws CDIException {
		setValue(value.getValueString());
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setValue(String)
	 */
	public void setValue(String expression) throws CDIException {
		Session session = (Session)(getTarget().getSession());
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarAssign var = factory.createMIVarAssign(miVar.getVarName(), expression);
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}

		// If the assign was succesfull fire a MIVarChangedEvent() for the variable
		// Note GDB will not fire an event for the changed variable we have to do it manually.
		MIVarChangedEvent change = new MIVarChangedEvent(var.getToken(), miVar.getVarName());
		mi.fireEvent(change);

		// Changing values may have side effects i.e. affecting other variables
		// if the manager is on autoupdate check all the other variables.
		// Note: This maybe very costly.
		if (this instanceof Register) {		
			// If register was on autoupdate, update all the other registers
			// assigning may have side effects i.e. affecting other registers.
			ICDIRegisterManager mgr = session.getRegisterManager();
			if (mgr.isAutoUpdate()) {
				mgr.update();
			}	
		} else if (this instanceof Expression) {
			// If expression was on autoupdate, update all the other expression
			// assigning may have side effects i.e. affecting other expressions.
			ICDIExpressionManager mgr = session.getExpressionManager();
			if (mgr.isAutoUpdate()) {
				mgr.update();
			}	
		} else {
			// FIXME: Should we always call the Variable Manager ?
			ICDIVariableManager mgr = session.getVariableManager();
			if (mgr.isAutoUpdate()) {
				mgr.update();
			}
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#isEditable()
	 */
	public boolean isEditable() throws CDIException {
		if (editable == null) {
			MISession mi = ((Session)(getTarget().getSession())).getMISession();
			CommandFactory factory = mi.getCommandFactory();
			MIVarShowAttributes var = factory.createMIVarShowAttributes(miVar.getVarName());
			try {
				mi.postCommand(var);
				MIVarShowAttributesInfo info = var.getMIVarShowAttributesInfo();
				if (info == null) {
					throw new CDIException("No answer");
				}
				editable = String.valueOf(info.isEditable());
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
		}
		return (editable == null) ? false : Boolean.getBoolean(editable);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#setFormat()
	 */
	public void setFormat(int format) throws CDIException {
		int fmt = Format.toMIFormat(format);
		MISession mi = ((Session)(getTarget().getSession())).getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIVarSetFormat var = factory.createMIVarSetFormat(miVar.getVarName(), fmt);
		try {
			mi.postCommand(var);
			MIInfo info = var.getMIInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#equals()
	 */
	public boolean equals(ICDIVariable var) {
		if (var instanceof Variable) {
			Variable variable = (Variable)var;
			return miVar.getVarName().equals(variable.getMIVar().getVarName());
		}
		return super.equals(var);
	}

}
