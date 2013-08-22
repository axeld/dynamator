/***********************************************************************
*   Copyright 2000-2003 by Jay Dunning.
*   All rights reserved.
*
*   Redistribution and use in source and binary forms, with or without
*   modification, are permitted provided that the following conditions 
*   are met:
*
*   1.  Redistributions of source code must retain the above copyright
*       notice, this list of conditions, and the following disclaimer.
*
*   2.  Redistributions in binary form must reproduce the above
*       copyright notice, this list of conditions, and the following
*       disclaimer in the documentation and/or other materials provided
*       with the distribution.
*
*   THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
*   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
*   OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*   DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR ANY CONTRIBUTORS
*   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
*   OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
*   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
*   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
*   OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
*   USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
*   SUCH DAMAGE.
***********************************************************************/

package dynamator;

/**
  Data represented by an attribute override tag in an annotations file.
**/
public
class AttributeOverride
{
//    private static final String rcsID_ = 
//        "$Id: AttributeOverride.java,v 1.6 2003/07/27 05:32:53 jaydunning Exp $";

    private String  keyType_;
    private String  key_;
    private String  name_;
    private String  newName_;
    private String  value_;
    private boolean valueIsRaw_;
    private String  ifExpression_;
    private boolean discard_;
    private boolean isFlag_;

    public
    AttributeOverride(
        String keyType,
        String key,
        String name,
        String newName,
        String value,
        boolean valueIsRaw,
        boolean isFlag,
        boolean discard,
        String ifExpression
        )
    {
        keyType_ = keyType;
        key_ = key;
        name_ = name;
        newName_ = newName;
        value_ = value;
        valueIsRaw_ = valueIsRaw;
        isFlag_ = isFlag;
        ifExpression_ = ifExpression;
        discard_ = discard;
    }

    /**
        The kind of key used to apply this attribute 
        (i.e. the parent element name in the annotations file:
        id, class, tag).
    **/
    public
    String
    keyType()
    {
        return keyType_;
    }

    /**
        The value of the key used to apply this attribute.
    **/
    public
    String
    key()
    {
        return key_;
    }

    /**
        The name of the HTML or XML attribute to be affected.
    **/
    public
    String
    name()
    {
        return name_;
    }

    /**
        The name of the HTML or XML attribute to be output.
        This value is changed by &lt;rename-attr&gt;.
    **/
    public
    String
    finalName()
    {
        return newName_ == null 
            ? name_
            : newName_;
    }

    /**
        The value to be given the attribute, or 'null' if the original
        value is to be used.
    **/
    public
    String
    value()
    {
        return value_;
    }

    /**
        True if the value should be output to the generated file as-is;
        false if the value is a program expression.
    **/
    public
    boolean
    valueIsRaw()
    {
        return valueIsRaw_;
    }

    /**
        True if the attribute is an HTML flag; that is, it has no value.
    **/
    public
    boolean
    isFlag()
    {
        return isFlag_;
    }

    /**
        A conditional expression used to determine whether the
        attribute should be output; or null if the attribute should
        always be output.
    **/
    public
    String
    ifExpression()
    {
        return ifExpression_;
    }

    /**
        True if the attribute should not be output.
    **/
    public
    boolean
    discard()
    {
        return discard_
            || "false".equals(ifExpression_);
    }
}
