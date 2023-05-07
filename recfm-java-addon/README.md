# recfm-maven-plugin / recfm-java-addon

## Table of Contents

* [1. Introduction](#1)
* [2. Preview](#2)
* [3. Plugin parameters details](#3)
* [4. Configuration YAML details](#4)
    * [4.1. Package level](#41)
    * [4.2. Class level](#42)
    * [4.3. Field level](#43)
        * [4.3.1. Alphanumeric](#431)
        * [4.3.2. Numeric](#432)
        * [4.3.3. Custom](#433)
        * [4.3.4. Domain](#434)
        * [4.3.5. Filler](#435)
        * [4.3.6. Constant](#436)
        * [4.3.7. Group](#437)
        * [4.3.8. Occurs](#438)
* [5. Special methods](#5)
    * [5.1. Deserialize `static T decode(String s)`](#51)
    * [5.2. Serialize `String encode()`](#52)
    * [5.3. Dump `String toString()`](#53)
    * [5.4. Validation methods](#54)
    * [5.5. Cast `static T of(FixRecord)`](#55)
    * [5.6. Clone `T copy()`](#56)

## <a name="1">1. Introduction</a>

[//]: # (può capitare che una serie di dati venga salvata su un file.)
It may happen that a series of data is saved to a file.

[//]: # (uno dei modi di serializzare il dato è convertirlo in una stringa posizionale.)
One way to serialize data is to convert it to a positional string.

[//]: # (ogni campo del dato viene trasformato in una stringa di lunghezza massima prefissata, queste stringhe vengono concatenate per formare una singola riga del file.)
Each data field is transformed into a string of pre-set maximum length, these strings are concatenated to form a single line of the file.

[//]: # (Per recuperare ogni campo del dato da una riga del file è necessario eseguire l'operazione inversa.)
To retrieve each data field from a line of the file it is necessary to perform the reverse operation.

[//]: # (non esiste una ricetta definitiva per questo tipo di deserializzazione della serializzazione)
There is no ultimate recipe for this type of serialization deserialization.

[//]: # (se ci sono pochi campi, il codice di serializzazione può essere scritto manualmente, ma al crescere dei campi questa soluzione è impraticabile.)
* If there are few fields, the serialization code can be written manually, but as the fields grow this solution is impractical.

[//]: # (Un'altra possibilità è aggiungere delle annotazioni sui campi del dato che forniscono le informazioni supplementari per permettere la serializzazione di ogni campo. un gruppo di classi dedicate, utilizza i valori dei campi e della relativa annotazione per serializzare deserializzare dei campi)
* Another possibility is to add annotations on the data fields which provide additional information to allow the serialization of each field. A set of dedicated classes, uses the values of fields and their annotation to serialize deserialize fields.

[//]: # (la soluzione proposta con questo plugin è quella di memorizzare le informazioni sul dato, le regole di serializzazione, e di validazione in un file di configurazione. il file viene dato in pasto al plugin, che ne verifica la consistenza, e genera la classe corrispondente ai dati con i metodi setter, getter, serializzazione, deserializzazione e validazione)
The solution proposed with this plugin is to store information about the data, serialization and validation rules in a configuration file.
The file is fed to the plugin, which checks its consistency, and generates the class corresponding to the data with the setter, getter, serialization, deserialization and validation methods.

[//]: # ()

Numeric fields are handled as strings that accept only numeric characters. For these fields it is also possible to request the generation of getters and setters with primitive numeric types.

## <a name="2">2. Preview</a>

Configuration plugin example:

~~~xml
<plugin>
    <groupId>io.github.epi155</groupId>
    <artifactId>recfm-maven-plugin</artifactId>
    <version>0.6.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <settings>
                    <setting>recfm-foo.yaml</setting>
                </settings>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>io.github.epi155</groupId>
            <artifactId>recfm-java-addon</artifactId>
            <version>0.6.1</version>
        </dependency>
    </dependencies>
</plugin>
~~~

Configuration file example:

~~~
packageName: org.example.sys.file
classes:
  - name: FooBody
    len: 543
    fields:
      - !Num { name: ibrKey  , at:   1, len:  11 }
      - !Num { name: ibrPrg  , at:  12, len:   6 }
      - !Num { name: recPrg  , at:  18, len:   9 }
      - !Abc { name: funcData, at:  27, len: 500 }
      - !Abc { name: reseData, at: 527, len:  17 }
~~~

Commandline

~~~
mvn recfm:generate
~~~

<a name="2-xmpl">Generated java class</a>

~~~java
package org.example.sys.file;
...

public class FooBody extends FixRecord {
    public static final int LRECL = 543;
    public FooBody() {...}    // empty constructor
    public static FooBody decode(String s) {...}    // de-serialize
    public static FooBody of(FixRecord r) {...}     // cast-like
    ...
    public String getIbrKey()  {...}
    public void setIbrKey(String s) {...}

    public String getIbrPrg() {...}
    public void setIbrPrg(String s) {...}

    public String getRecPrg() {...}
    public void setRecPrg(String s) {...}

    public String getFuncData() {...}
    public void setFuncData(String s) {...}

    public String getReseData() {...}
    public void setReseData(String s) {...}

    public FooBody copy() {...}     // clone - deep copy
    public String encode() {...}    // serialize
    public String toString() {...}  // human readable dump
}
~~~

Dependencies required by the user code

~~~xml
        <dependency>
            <groupId>io.github.epi155</groupId>
            <artifactId>recfm-java-lib</artifactId>
            <version>0.6.1</version>
        </dependency>
~~~

## <a name="3">3. Plugin parameters details</a>

Parameters

`generateDirectory` or property `maven.recfm.generateDirectory`
: Indicates the base directory from which to generate packages, default value is `${project.build.directory}/generated-sources/recfm`,
ie **`target/generated-sources/recfm`**

`settingsDirectory` or property `maven.recfm.settingsDirectory`
: Indicates the base directory that contains the configuration files, default value
is `${project.build.resources[0].directory}`, ie **`src/main/resources`**

**`settings`**
: List of configuration files (required).

`doc` or property `maven.recfm.doc`
: Indicates whether or not to generate the javadoc documentation on setters and getters, default is **`false`**.

`align` or property `maven.recfm.align`
: Indicates the minimum alignment of numeric fields when numeric representation is required. The Default value is **4**,
ie setters and getters are adapted to `int` or `long` depending on the length of the field. Using 2 also uses `short`,
and using 1 also uses `byte`.

`enforceGetter` or property `maven.recfm.enforceGetter`
: indicates whether to check the value before returning the value. Default value is **`true`**.

`enforceSetter` or property `maven.recfm.enforceSetter`
: indicates whether to check the supplied value before setting it in the record. Default value is **`true`**.

property `maven.recfm.addCompileSourceRoot`
: If set to **true** (default), adds target directory as a compile source root of this Maven project.

property `maven.recfm.addTestCompileSourceRoot`
: If set to true, adds target directory as a test compile source root of this Maven project. Default value is **false**.

## <a name="4">4. Configuration YAML details</a>

In general, we can have multiple configuration files. Each configuration file can define multiple classes within a
package.

### <a name="41">4.1. Package level</a>

The first thing to define is the package name

~~~yml
packageName: org.example.sys.file
~~~

Then we can assign some predefined behaviors, these behaviors can also be defined at the field level, in this case they
are defined at the package level

~~~yml
defaults:
  abc:
    check: Ascii
  fil:
    check: None
    fill: 0
  cus:
    align: LFT
    check: Ascii
    init: ' '
    pad: ' '
~~~

`defaults.abc.check` indicates which checks to perform on alphanumeric fields, the default value is **Ascii**.

`defaults.fil.check` indicates which checks to perform on Filler fields, the default value is **None**.

`defaults.fil.fill` is the fill character to use for fields of type *Filler*, the default value is **0** (ie **\u0000**)

`defaults.cus.align` indicates which the align rule on *Custom* fields, the default value is **LFT** (ie **left**).

`defaults.cus.check` indicates which checks to perform on *Custom* fields, the default value is **Ascii**.

`defaults.cus.init` is the initializer character to use for fields of type *Custom*, the default value is **' '** (ie **SPACE**)

`defaults.cus.pad` is the pad character to use for fields of type *Custom*, the default value is **' '** (ie **SPACE**)

After that we can define the single classes

~~~yml
classes:
  - {...}
~~~

### <a name="42">4.2. Class level</a>

A string is associated with each class. At the class level the following parameters can be defined:

~~~yml
classes:
  - name: FooBody
    length: 543
    onOverflow: Trunc
    onUnderflow: Pad
    fields:
      - {...}
~~~

* `classes[].name` is the name of the class.
* `classes[].length` is the length of the string (`length` can be abbreviated to `len`)
* `classes[].onOverflow` indicates how to behave in the deserialization phase if the length of the supplied string is
  greater than that expected, default value is **Trunc** (ie extra characters are ignored), the alternate value is **
  Error** that throws a *RecordOverflowException*.
* `classes[].onUnderflow` indicates how to behave in the deserialization phase if the length of the supplied string is
  less than the expected one, default value is **Pad** (ie ..), the alternate value is **Error** that throws a *
  RecordUnderflowException*.

### <a name="43">4.3. Field level</a>

It is possible to define various types of fields, each of which has specific attributes. For this reason a specific json
tag is used for each type, Let's see them in detail one by one.

The entire area of the class or of a group of it must be associated with a field. If a part of the area is not needed it
should be defined as a filler.

#### <a name="431">4.3.1. Alphanumeric </a>

Tag for alphanumeric field is `Abc`, the possible attributes are:

| attribute                | alt |  type   | note                               |
|--------------------------|-----|:-------:|------------------------------------|
| [offset](#fld.offset)    | at  |   int   | **required**                       |
| [length](#fld.length)    | len |   int   | **required**                       |
| [name](#fld.name)        |     | String  | **required**                       |
| [redefines](#fld.redef)  | red | boolean | default `false`                    |
| [audit](#fld.audit)      |     | boolean | default `false`                    |
| [onOverflow](#fld.ovfl)  |     |  [^1]   | default `Trunc`                    |
| [onUnderflow](#fld.unfl) |     |  [^2]   | default `Pad`                      |
| [padChar](#fld.pchr)     | pad |  char   | default value `' '`                |
| [check](#fld.chk)        | chk |  [^3]   | default value `defaults.abc.check` |

[^1]: Overflow domain: Trunc, Error
[^2]: Underflow domain: Pad, Error
[^3]: CheckA domain: None, Ascii, Latin1, Valid

Some attributes also have a shortened form. The meaning of some attributes is immediate.

The <ins id='fld.offset'>offset</ins> attribute indicates the starting position of the field (starting from 1).

The <ins id='fld.length'>length</ins> attribute indicates the length of the field.

The <ins id='fld.name'>name</ins> attribute indicates the name of the field.

~~~yml
classes:
  - name: Foo
    length: 75
    fields:
      - !Abc { name: huey , at:  1, len: 25 }
      - !Abc { name: dewey, at: 26, len: 25 }
      - !Abc { name: louie, at: 51, len: 25 }
~~~

<a name="431-xmpl">Generated setter and getter</a>

~~~java
    public String getHuey(){...}
    public void setHuey(String s){...}

    public String getDewey(){...}
    public void setDewey(String s){...}

    public String getLouie(){...}
    public void setLouie(String s){...}
~~~

The <ins id='fld.redef'>redefines</ins> attribute indicates that the field is a redefinition of an area, this field will
not be considered in the overlay checks

~~~yml
  - name: Foo
    length: 10
    fields:
      - !Abc { name: isoDate , at: 1, len: 10 }   # yyyy-MM-dd
      - !Num { name: year    , at: 1, len: 4, red: true }
      - !Num { name: month   , at: 6, len: 2, red: true }
      - !Num { name: day     , at: 9, len: 2, red: true }
~~~

To introduce the <ins id='fld.audit'>audit</ins> attribute see section [5.4](#54).

<ins id='fld.ovfl'>onOverflow</ins> indicates what to do if you try to set a value whose length is greater than the
defined one. It is possible to set `Trunc` and `Error`, in the first two cases the value is truncated, respectively to
the left or to the right, in the last case an exception is thrown.

<ins id='fld.unfl'>onUnderflow</ins> indicates what to do if you try to set a value whose length is less than the defined one. It is possible to set `Pad` and `Error`, in the first two cases the value is padded respectively to the left or to the right, in the last case an exception is thrown.

<ins id='fld.pchr'>padchar</ins> indicates the character to use for padding, in case of underflow.

<ins id='fld.chk'>check</ins> indicates which checks to perform in the *validate* or *audit* phase. The following values are available:

`None`
: no check will be performed

`Ascii`
: only ascii characters are accepted, control characters are not accepted (95 characters only)

`Latin1`
: only latin1 (ISO-8859-1) characters are accepted, control characters are not accepted (190 characters only)

`Valid`
: characters that pass the `Character.isDefined(c)` test are accepted

#### <a name="432">4.3.2. Numeric </a>

Tag for numeric field is `Num`, many attributes have the same meaning as in the alphanumeric case, the padding character
is necessarily 0, the control is necessarily that the characters are numeric, the possible attributes are:

| attribute                 | alt |  type   | note                  |
|---------------------------|-----|:-------:|-----------------------|
| [offset](#fld.offset)     | at  |   int   | **required**          |
| [length](#fld.length)     | len |   int   | **required**          |
| [name](#fld.name)         |     | String  | **required**          |
| [redefines](#fld.redef)   | red | boolean | default `false`       |
| [audit](#fld.audit)       |     | boolean | default `false`       |
| [onOverflow](#fld.ovfl)   |     |  [^1]   | default `Trunc`       |
| [onUnderflow](#fld.unfl)  |     |  [^2]   | default `Pad`         |
| [numericAccess](#fld.num) | num | boolean | default value `false` |

<ins id='fld.num'>numericAccess</ins> indicates whether to generate the numeric setters and getters for the field, in
addition to the alphanumeric ones. Numeric getters are prefixed with the return type.

~~~yml
  - name: Foo
    length: 10
    fields:
      - !Num { name: year , at: 1, len: 4, num: true }
      - !Fil { at: 5, len: 1 }
      - !Num { name: month, at: 6, len: 2, num: true }
      - !Fil { at: 8, len: 1 }
      - !Num { name: date , at: 9, len: 2, num: true }
~~~

<a name="432-xmpl">Generated java for *year* field</a>

~~~java
    public String getYear(){...}
    public void setYear(String s){...}
    public int intYear(){...}
    public void setYear(int n){...}
~~~

#### <a name="433">4.3.3. Custom </a>
Tag for custom field is `Cus`, a custom field is an extension of an alphanumeric field, with some additional parameters

| attribute                | alt |  type   | note                         |
|--------------------------|-----|:-------:|------------------------------|
| [offset](#fld.offset)    | at  |   int   | **required**                 |
| [length](#fld.length)    | len |   int   | **required**                 |
| [name](#fld.name)        |     | String  | **required**                 |
| [redefines](#fld.redef)  | red | boolean | default `false`              |
| [audit](#fld.audit)      |     | boolean | default `false`              |
| [onOverflow](#fld.ovfl)  |     |  [^1]   | default `Trunc`              |
| [onUnderflow](#fld.unfl) |     |  [^2]   | default `Pad`                |
| [padChar](#fld.pchr)     | pad |  char   | default `defaults.cus.pad`   |
| [initChar](#fld.ichr)    | ini |  char   | default `defaults.cus.init`  |
| [check](#fld.ichk)       | chk |  [^4]   | default `defaults.cus.check` |
| [regex](#fld.regx)       |     | String  | default *null*               |
| [align](#fld.ialign)     |     |  [^5]   | default `defaults.cus.align` |

[^4]: CheckC domain: None, Ascii, Latin1, Valid, Digit, DigitOrBlank
[^5]: AlignC domain: LFT, RGT

<ins id='fld.ichr'>initChar</ins> indicates the character to use to initialize the field when the empty constructor is
used.

<ins id='fld.ichk'>check</ins> indicates which checks to perform in the *validate* or *audit* phase. The following values
are available:

`None` .. `Valid`
: see [alphanumeric](#fld.chk) case

`Digit`
: only numeric characters from 0 to 9 are accepted

`DigitOrBlank`
: digit or all space characters is accepted

<ins id='fld.regx'>regex</ins> if present indicates that the value must satisfy the regular expression, this check
overrides the [check](#fld.ichk) one. Regex control, on setter, is performed after any pad / truncate normalizations

<ins id='fld.ialign'>align</ins> indicates the direction to align the field in case the supplied length is different from
the available one.

#### <a name="434">4.3.4. Domain </a>

Tag for domain field is `Dom`, a domain field can only take a limited number of values, the possible attributes are:

| attribute               | alt |   type   | note            |
|-------------------------|-----|:--------:|-----------------|
| [offset](#fld.offset)   | at  |   int    | **required**    |
| [length](#fld.length)   | len |   int    | **required**    |
| [name](#fld.name)       |     |  String  | **required**    |
| [redefines](#fld.redef) | red | boolean  | default `false` |
| [audit](#fld.audit)     |     | boolean  | default `false` |
| [items](#fld.items)     |     | String[] | **required**    |

<ins id='fld.items'>items</ins> indicates the list of possible values that the field can assume. All values supplied must have the expected length for the field. The first value supplied will be used to initialize the field.

~~~yml
  - name: Foo
    length: 20
    fields:
      - !Abc { name: date    , at:  1, len: 10 }
      - !Num { name: amount  , at: 11, len:  7, num: true }
      - !Dom { name: currency, at: 18, len:  3, items: [ EUR, USD, CHF, GBP, JPY ] }
~~~



#### <a name="435">4.3.5. Filler </a>

Tag for filler field is `Fil`, a filler is an area we are not interested in, neither getters nor setters are generated
for it, the possible attributes are:

| attribute             | alt | type | note                               |
|-----------------------|-----|:----:|------------------------------------|
| [offset](#fld.offset) | at  | int  | **required**                       |
| [length](#fld.length) | len | int  | **required**                       |
| [fillChar](#fld.fill) |     | char | default value `defaults.fil.fill`  |
| [check](#fld.chk)     |     | [^3] | default value `defaults.fil.check` |

<ins id='fld.fill'>fillChar</ins> indicates the character to use to initialize the area

#### <a name="436">4.3.6. Constant </a>

Tag for constant field is `Val`, even for a constant field the setters and getters are not generated, the controls
verify that the present value coincides with the set one, the possible attributes are:

| attribute             | alt |  type   | note            |
|-----------------------|-----|:-------:|-----------------|
| [offset](#fld.offset) | at  |   int   | **required**    |
| [length](#fld.length) | len |   int   | **required**    |
| [value](#fld.val)     | val | String  | **required**    |
| [audit](#fld.audit)   |     | boolean | default `false` |

<ins id='fld.val'>value</ins> indicates the value with which to initialize the area

#### <a name="437">4.3.7. Group </a>

Tag for group field is `Grp`, a group allows you to group multiple fields in order to structure the area, the possible
attributes are:

| attribute               | alt |  type   | note                      |
|-------------------------|-----|:-------:|---------------------------|
| [offset](#grp.offset)   | at  |   int   | **required**              |
| [length](#grp.length)   | len |   int   | **required**              |
| [name](#grp.name)       |     | String  | **required**              |
| [redefines](#grp.redef) | red | boolean | default `false`           |
| [fields](#grp.flds)     |     |  array  | **required** child fields |

The <ins id='grp.offset'>offset</ins> attribute indicates the starting position of the group (starting from 1).

The <ins id='grp.length'>length</ins> attribute indicates the length of the group.

The <ins id='grp.name'>name</ins> attribute indicates the name of the group.

The <ins id='grp.redef'>redefines</ins> attribute indicates that the group is a redefinition of an area, this group will
not be considered in the overlay checks

The <ins id='grp.flds'>fields</ins> attribute indicates a definition list of fields

Group definition example (mixed yaml/json):

~~~yml
  - name: BarReq
    len: 19324
    fields:
      - !Grp { name: transactionArea       , at:  1, len: 12,
               fields: [
                 !Abc { name: cdTransazione, at:  1, len:  9 },
                 !Num { name: esitoAgg     , at: 10, len:  1 },
                 !Num { name: esitoCompl   , at: 11, len:  1 },
                 !Val { val: "\n"          , at: 12, len:  1 }
               ] }
      - ...
~~~

or (yaml style):

~~~yml
  - name: BarReq
    len: 19324
    fields:
      - !Grp 
        name: transactionArea 
        at:  1 
        len: 12
        fields:
          - !Abc { name: cdTransazione, at:  1, len:  9 }
          - !Num { name: esitoAgg     , at: 10, len:  1 }
          - !Num { name: esitoCompl   , at: 11, len:  1 }
          - !Val { val: "\n"          , at: 12, len:  1 }
      - ...
~~~


<a name="437-xmpl">Group usage example:</a>

~~~java
        val bar=new BarReq();
        bar.transactionArea().setCdTransazione("TR00");
        bar.transactionArea().setEsitoAgg("0");
        val esitoComplTransaction=bar.transactionArea().getEsitoCompl();
~~~

Group fields can be handled with a lambda function (java8+)

~~~java
        val bar=new BarReq();
        bar.withTransactionArea(it -> {
            it.setCdTransazione("TR00");
            it..setEsitoAgg("0");
        });
~~~

or with an anonymous class

~~~java
        val bar=new BarReq();
        bar.withTransactionArea(new WithAction<TransactionArea>(){
            public void call(BarReq.TransactionArea it) {
                it.setCdTransazione("TR00");
                it.setEsitoAgg("0");
            }
        });
~~~


In general two fields can not have the same name (only one of them would be referenceable), but a group is something other than a field. In the Java implementation there are no referencing problems if a field and a group use the same name. But in the Scala implementation this is not possible.

A definition like this generates the java class successfully, but fails to generate the scala class.
~~~yaml
  - name: FooDate
    length: 10
    fields:
      - !Abc { name: date   , at: 1, len: 10 }
      - !Grp { name: date   , at: 1, len: 10, red: true, fields: [
          !Num { name: year , at: 1, len:  4, num: true },
          !Fil {              at: 5, len:  1 },
          !Num { name: month, at: 6, len:  2, num: true },
          !Fil {              at: 8, len:  1 },
          !Num { name: day  , at: 9, len:  2, num: true }
      ]}
~~~


#### <a name="438">4.3.8. Occurs </a>

Tag for occurs field is `Occ`, an occurs is basically a repeated group, it is defined with the group data of the first
occurrence and the number of occurrences, the possible attributes are:

| attribute               | alt |  type   | note                      |
|-------------------------|-----|:-------:|---------------------------|
| [offset](#occ.offset)   | at  |   int   | **required**              |
| [length](#occ.length)   | len |   int   | **required**              |
| [name](#occ.name)       |     | String  | **required**              |
| [redefines](#occ.redef) | red | boolean | default `false`           |
| [fields](#occ.flds)     |     |  array  | **required** child fields |
| [times](#occ.times)     | x   |   int   | **required** occurrences  |

The <ins id='occ.offset'>offset</ins> attribute indicates the starting position of the first group (starting from 1).

The <ins id='occ.length'>length</ins> attribute indicates the length of a single group.

The <ins id='occ.name'>name</ins> attribute indicates the name of the group.

The <ins id='occ.redef'>redefines</ins> attribute indicates that the group is a redefinition of an area, this group will
not be considered in the overlay checks

The <ins id='occ.flds'>fields</ins> attribute indicates a definition list of fields, the offsets of the fields are those
relative to the first group

The <ins id='occ.times'>times</ins> attribute indicates the number of times the group is repeated

Occurs definition example:

~~~yml
  - name: FooResp
    len: ...
    fields:
      - ...
      - !Occ { name: errItem                  , at: 92, len: 20, x: 25,
               fields: [
                 !Abc { name: applicationId   , at: 92, len: 2 },
                 !Abc { name: errorCodeSource , at: 94, len: 5 },
                 !Abc { name: errorCodeTarget , at: 99, len: 4 },
                 !Abc { name: aliasId         , at: 103, len: 8 },
                 !Fil { at: 111, len: 1 }
               ] }
      - ...
~~~

<a name="438-xmpl">Occurs usage example:</a>

~~~java
    val resp=new FooResp();
    ...
    resp.errItem(1).setApplicationId("05");
    resp.errItem(1).setErrorCodeSource("91302");
    resp.errItem(2).setApplicationId("07");
    resp.errItem(2).setErrorCodeSource("38000");
~~~

## <a name="5">5. Special methods</a>

In addition to the setters and getters, the deserialization, serialization and dump methods of the class are defined

## <a name="51">5.1. Deserialize  `static T decode(String s)`</a>

This method is used to create the class from a string. The content of the string is not automatically validated it is
advisable to validate the class with the appropriate methods before using the class. The class can also be instantiated
using the empty constructor, in this case all fields are initialized to default values.

## <a name="52">5.2. Serialize  `String encode()`</a>

`encode` is the serialization methods, it transforms the class into the string that represents it.

## <a name="53">5.3. Dump `String toString()`</a>

The toString method is used to dump the class. The list of fields is shown and for each field the offset, length and
value are shown.

## <a name="54">5.4. Validation methods</a>

Each generated class defines the validation methods

~~~java
    boolean validateFails(FieldValidateHandler handler);
    boolean auditFails(FieldValidateHandler handler);
~~~

The *validateFails* method performs checks on all fields, the *auditFails* method only on the fields marked with the `audit`
attribute. If a field is redefined it is not considered in the validation checks.

The `FieldValidateHandler` interface is simply

~~~java
public interface FieldValidateHandler {
    void error(FieldValidateError fieldValidateError);
}
~~~

When an error occurs, the `error` method is called with fieldValidateError detail.

`FieldValidateError` in an interface with getter method
~~~java
public interface FieldValidateError {
    String name();          // field name, if named field
    int offset();           // field offset
    int length();           // field length
    String value();         // field value
    Integer column();       // column of wrong char, if single one
    ValidateError code();   // error code
    Character wrong();      // wrong char, if single one
    String message();
}
~~~

The possible types of errors are

~~~java
public enum ValidateError {
    NotAscii, NotBlank, NotDomain, NotEqual, NotLatin, NotMatch, NotNumber, NotValid
}
~~~

with the meaning

`NotAscii`
: non-ASCII character in an alphanumeric field where Ascii control is required

`NotBlank`
: not Blank character in custom field where DigitOrBlank control is required

`NotDomain`
: not Domain value in domain field

`NotEqual`
: value other than expected in a constant field

`NotLatin`
: non-Latin1 (ISO-8859-1) character in an alphanumeric field where the Latin1 control is required

`NotMatch`
: value does not match regex pattern

`NotNumber`
: non-numeric character in a numeric field

`NotValid`
: character that fails the `Character.isDefined(c)` test in an alphanumeric field where the Valid check is required

## <a name="55">5.5. Cast `static T of(FixRecord)`</a>

The `of` method is used to create a new FixRecord class with the same payload as the original one. The payload is the same, in the sense that a change to the new class also changes the original one. The method is like a cast, not a copy.

## <a name="56">5.6. Clone `T copy()`</a>

The `copy` method creates a copy of the original class.