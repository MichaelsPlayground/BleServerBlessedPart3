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

Unfortunately the "official" and maybe updated version of the specifiaction is available for bluetooth.com 
members but there is a GitHub-repo that provides to (older / outdated ? ) versions.

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

4) what do we need to implement in the Battery Service ?

See page 8 of the Battery Service Specification (BAS_SPEC_V10.pdf):

![BAS](docs/battery_service_page_08.png?raw=true)

There is only 1 characteristic named as Battery Level with a (mandatory) read access and an 
optional "notify" property.




5) do you like to use the "real" battery level of your device ?

````plaintext
SDK >= 21

BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); 

````

https://developer.android.com/reference/android/os/BatteryManager.html

https://developer.android.com/training/monitoring-device-state/battery-monitoring

