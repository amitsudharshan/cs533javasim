# Introduction #

below will be a work in progress test case asm file for our simulator

# Details #
```
#data
$Basketball 0x32
$Baseball 0x16
$Sports 0x0
$NegativeOne 0xFFFFFFFF

#instructions
load r1 $Basketball // r1 = m[$Basketball]
load r2 $Baseball // r2 = m[$Baseball]
load r3 $NegativeOne

#loop
add r3 r3 r2  // r4 = r2-1
jz r3 #loop
store $Sports r3 // M[Sports] = r3
```