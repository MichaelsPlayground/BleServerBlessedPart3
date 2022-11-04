# how to create a Battery Service for Bluetooth Low Energy devices

To create a "new" service it is a good idea to check if a service and its UUID is already defined.

1) Website to get an actual overview: https://www.bluetooth.com/specifications/specs/

Go to "Battery Service" and download the Battery Service 1.0.pdf document:

https://www.bluetooth.org/docman/handlers/downloaddoc.ashx?doc_id=245138
 
BAS_SPEC_V10.pdf

2) Now we knew that the Battery Service is a official service we need the UUIDs for the service and the characteristic(s).

Go to this page: https://www.bluetooth.com/specifications/assigned-numbers/

and download the "Assigned Numbers Document"

assigned_numbers_release.pdf

Open the PDF and search for "Battery":

The Battery Service has: 0x180F means 0000180F-0000-1000-8000-00805f9b34fb as full UUID

The characteristic is the Battery Level: 0x2A19 means 00002A19-0000-1000-8000-00805f9b34fb as full UUID

3) The last information we need to know is how the data is formatted with the characteristic above ?

If you search for "battery_service.xml" the first link I found refers to 

https://github.com/oesmith/gatt-xml/blob/master/org.bluetooth.service.battery_service.xml

Unfortunately the "official" and maybe outdated version of the specification is available for bluetooth.com 
for members only but there is a GitHub-repo that provides (older / outdated ? ) versions.

```plaintext
<?xml version="1.0" encoding="utf-8"?>
<!-- Copyright 2011 Bluetooth SIG, Inc. All rights reserved. -->
<Service xsi:noNamespaceSchemaLocation="http://schemas.bluetooth.org/Documents/service.xsd"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
name="Battery Service" type="org.bluetooth.service.battery_service"
uuid="180F" last-modified="2011-12-12">
  <InformativeText>
    <Abstract>The Battery Service exposes the state of a battery
    within a device.</Abstract>
    <Summary>The Battery Service exposes the Battery State and
    Battery Level of a single battery or set of batteries in a
    device.</Summary>
  </InformativeText>
  <Dependencies>
    <Dependency>This service has no dependencies on other
    GATT-based services.</Dependency>
  </Dependencies>
  <GATTRequirements>
    <Requirement subProcedure="Read Characteristic Descriptors">
    Mandatory</Requirement>
    <Requirement subProcedure="Notifications">C1: Mandatory if the
    Battery Level characteristic properties supports notification,
    otherwise excluded.</Requirement>
    <Requirement subProcedure="Write Characteristic Descriptors">
    C1: Mandatory if the Battery Level characteristic properties
    supports notification, otherwise excluded.</Requirement>
  </GATTRequirements>
  <Transports>
    <Classic>true</Classic>
    <LowEnergy>true</LowEnergy>
  </Transports>
  <ErrorCodes></ErrorCodes>
  <Characteristics>
    <Characteristic name="Battery Level"
    type="org.bluetooth.characteristic.battery_level">
      <InformativeText>The Battery Level characteristic is read
      using the GATT Read Characteristic Value sub-procedure and
      returns the current battery level as a percentage from 0% to
      100%; 0% represents a battery that is fully discharged, 100%
      represents a battery that is fully charged.</InformativeText>
      <Requirement>Mandatory</Requirement>
      <Properties>
        <Read>Mandatory</Read>
        <Write>Excluded</Write>
        <WriteWithoutResponse>Excluded</WriteWithoutResponse>
        <SignedWrite>Excluded</SignedWrite>
        <ReliableWrite>Excluded</ReliableWrite>
        <Notify>Optional</Notify>
        <Indicate>Excluded</Indicate>
        <WritableAuxiliaries>Excluded</WritableAuxiliaries>
        <Broadcast>Excluded</Broadcast>
      </Properties>
      <Descriptors>
        <Descriptor name="Characteristic Presentation Format"
        type="org.bluetooth.descriptor.gatt.characteristic_presentation_format">

          <Requirement>if_multiple_service_instances</Requirement>
          <Properties>
            <Read>Mandatory</Read>
            <Write>Excluded</Write>
          </Properties>
        </Descriptor>
        <Descriptor name="Client Characteristic Configuration"
        type="org.bluetooth.descriptor.gatt.client_characteristic_configuration">

          <Requirement>
          if_notify_or_indicate_supported</Requirement>
          <Properties>
            <Read>Mandatory</Read>
            <Write>Mandatory</Write>
          </Properties>
        </Descriptor>
      </Descriptors>
    </Characteristic>
  </Characteristics>
</Service>
```

4) and there is something like a Characteristic Presentation Format

```plaintext
From log:
DeviceId = D3:CC:CB:F7:AC:09
ServiceId = cc4a6a80-51e0-11e3-b451-0002a5d5c51b
CharacteristicId = 835ab4c0-51e4-11e3-a5bd-0002a5d5c51b
DescriptorId =00002904-0000-1000-8000-00805f9b34fb
Value.Length = 7
Value = 0EFFA527010000
where descriptor 0x2904 aka "Characteristic Presentation Format" describes the sensor's pressure characteristic:

0E = reading is a signed 16-bit integer
FF = exponent is -1 = scale/multiply 16-bit reading by 10^-1 (ie, reading is in 0.1 PSI)
A527 = 0x27A5 = reading is in org.bluetooth.unit.pressure.pound_force_per_square_inch
010000 = description "unknown"

```

https://github.com/oesmith/gatt-xml/blob/master/org.bluetooth.descriptor.gatt.characteristic_presentation_format.xml
```plaintext
<?xml version="1.0" encoding="utf-8"?>
<!-- Copyright 2011 Bluetooth SIG, Inc. All rights reserved. -->
<Descriptor xsi:noNamespaceSchemaLocation="http://schemas.bluetooth.org/Documents/descriptor.xsd"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
type="org.bluetooth.descriptor.gatt.characteristic_presentation_format"
uuid="2904" name="Characteristic Presentation Format">
  <InformativeText>
    <Abstract>The Characteristic Presentation Format descriptor
    defines the format of the Characteristic Value.</Abstract>
    <InformativeDisclaimer />
    <Summary>One or more Characteristic Presentation Format
    descriptors may be present. If multiple of these descriptors
    are present, then a Aggregate Formate descriptor is present.
    This descriptor is read only and does not require
    authentication or authorization to read. This descriptor is
    composed of five parts: format, exponent, unit, name space and
    description. The Format field determines how a single value
    contained in the Characteristic Value is formatted. The
    Exponent field is used with interger data types to determine
    how the Characteristic Value is furhter formatted. The actual
    value = Characteristic Value * 10^Exponent.</Summary>
    <Examples>
      <Example>When encoding an IPv4 address, the uint32 Format
      type is used.</Example>
      <Example>When encoding an IPv6 address, the uint128 Format
      type is used.</Example>
      <Example>When encoding a Bluetooth address (BD_ADDR), the
      uint48 Format type is used.</Example>
      <Example>For a Characteristic Value of 23 and an Exponent of
      2, the actual value is 2300</Example>
      <Example>For a Characteristi Value of 3892 and an Exponent of
      -3, the actual value is 3.892</Example>
    </Examples>
  </InformativeText>
  <Value>
    <Field name="Format">
      <Requirement>Mandatory</Requirement>
      <Format>8bit</Format>
      <Minimum>0</Minimum>
      <Maximum>27</Maximum>
      <Enumerations>
        <Enumeration key="0" value="Reserved For Future Use" />
        <Enumeration key="1" value="Boolean" />
        <Enumeration key="2" value="unsigned 2-bit integer" />
        <Enumeration key="3" value="unsigned 4-bit integer" />
        <Enumeration key="4" value="unsigned 8-bit integer" />
        <Enumeration key="5" value="unsigned 12-bit integer" />
        <Enumeration key="6" value="unsigned 16-bit integer" />
        <Enumeration key="7" value="unsigned 24-bit integer" />
        <Enumeration key="8" value="unsigned 32-bit integer" />
        <Enumeration key="9" value="unsigned 48-bit integer" />
        <Enumeration key="10" value="unsigned 64-bit integer" />
        <Enumeration key="11" value="unsigned 128-bit integer" />
        <Enumeration key="12" value="signed 8-bit integer" />
        <Enumeration key="13" value="signed 12-bit integer" />
        <Enumeration key="14" value="signed 16-bit integer" />
        <Enumeration key="15" value="signed 24-bit integer" />
        <Enumeration key="16" value="signed 32-bit integer" />
        <Enumeration key="17" value="signed 48-bit integer" />
        <Enumeration key="18" value="signed 64-bit integer" />
        <Enumeration key="19" value="signed 128-bit integer" />
        <Enumeration key="20"
        value="IEEE-754 32-bit floating point" />
        <Enumeration key="21"
        value="IEEE-754 64-bit floating point" />
        <Enumeration key="22" value="IEEE-11073 16-bit SFLOAT" />
        <Enumeration key="23" value="IEEE-11073 32-bit FLOAT" />
        <Enumeration key="24" value="IEEE-20601 format" />
        <Enumeration key="25" value="UTF-8 string" />
        <Enumeration key="26" value="UTF-16 string" />
        <Enumeration key="27" value="Opaque Structure" />
        <Reserved start="28" end="255"></Reserved>
      </Enumerations>
    </Field>
    <Field name="Exponent">
      <Requirement>Mandatory</Requirement>
      <Format>sint8</Format>
    </Field>
    <Field name="Unit">
      <InformativeText>The Unit is a UUID.</InformativeText>
      <Requirement>Mandatory</Requirement>
      <Format>uint16</Format>
    </Field>
    <Field name="Namespace">
      <InformativeText>The Name Space field is used to indentify
      the organization that is responsible for defining the
      enumerations for the description field.</InformativeText>
      <Requirement>Mandatory</Requirement>
      <Format>8bit</Format>
      <Minimum>0</Minimum>
      <Maximum>1</Maximum>
      <Enumerations>
        <Enumeration key="1"
        value="Bluetooth SIG Assigned Numbers" />
        <ReservedForFutureUse start="2" end="255" />
      </Enumerations>
    </Field>
    <Field name="Description">
      <InformativeText>The Description is an enumerated value from
      the organization identified by the Name Space
      field.</InformativeText>
      <Requirement>Mandatory</Requirement>
      <Format>16bit</Format>
    </Field>
  </Value>
</Descriptor>
```


5) what do we need to implement in the Battery Service ?

See page 8 of the Battery Service Specification (BAS_SPEC_V10.pdf):

![BAS](docs/battery_service_page_08.png?raw=true)

There is only 1 characteristic named as Battery Level with a (mandatory) read access and an 
optional "notify" property.

Now we need the data format of the Battery Level, see the specification.xml (org.bluetooth.service.battery_service.xml) 

*The Battery Level characteristic is read using the GATT Read Characteristic Value sub-procedure and 
returns the current battery level as a percentage from 0% to 100%; 0% represents a battery that is 
fully discharged, 100% represents a battery that is fully charged.*

As a value from 0 to 100 can be transported within 1 byte we just need a byte array of 1 byte length. 

... is it neccessary to stay within the specification ? Short answer: no, long answer: it depends...

When working with "official" UUIDs we should follow the specification because most professional apps  
know how to handle the most common services and characteristics and try to read and display the data 
"as it should be".

If our dedicated BatteryService.java is called for a **onCharacteristicRead** the server should respond 
with a 1 byte long byte array. 

6) do you like to use the "real" battery level of your device ?

````plaintext
SDK >= 21

BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); 

````

https://developer.android.com/reference/android/os/BatteryManager.html

https://developer.android.com/training/monitoring-device-state/battery-monitoring

