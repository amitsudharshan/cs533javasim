//This file is being used to test
//The CandS instruction

.data
// this is the lock variable. It is locked with value 1 and unlocked with value 0
$lock 0
$protectedCounter 0x15


.instructions
#startInstructions


//Make r1 the address register
lui r1 U$lock
ori r1 r1 L$lock

//zero out r2. This will be the compare register
lui r2 U0x0
ori r2 r2 L0x0
//load address of the protected counter into r4
lui r4 U$protectedCounter
ori r4 r4 L$protectedCounter

// load heap address into r7
lui r7 U$heap
ori r7 r7 L$heap

//DO NOT OVERWRITE ANY OF THESE REGISTERS IN CODE BELOW AS THEY HOLD GLOBAL VALUES

#startLoop
//we also must reset r3 to be 0x1 as it is the compare register
lui r3 U0x0
ori r3 r3 L0x01
// if M[$Lock] == 0 Then M[$Lock] = 1
cas r1 r2 r3
beq r3 r2 #criticalSection

beq r3 r3 #startLoop

#criticalSection
//load r6 = M[r4] counter into register
lw r4 r6
blez r6 #unlockLock
//add r6 to -1 (the add immediate instruction sign extends)
addi r6 r6 0xFFFF
sw r4 r6

#unlockLock
//r2 is zero in this case and so we can safely set M[$lock] = 0 which unlocks
sw r1 r2
// if counter is zero then goto done
blez r6 #done
//else loop back around
beq r6 r6 #startLoop

#done
halt

.startpc

#startInstructions
