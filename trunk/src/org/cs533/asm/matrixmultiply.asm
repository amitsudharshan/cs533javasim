//This file is bt org.cs533.newprocessor.assembler.Assembler.getFullImage(Assembler.java:95)eing used to test a producer consumer scenario
// using a single lock queue

.data
    // this is the lock variable. It is locked with value 1 and unlocked with value 0
    $lock 0
   $firstMatrixRows 10
   $firstMatrixColumns 10
   $secondMatrixRows 10
   $secondMatrixColumns 10
   $resultMatrixRows 10
   $resultMatrixColumns 10
   
   $currRow 0
   $currCol 0

    $firstMatrix[100]
    //10x15 matrix
    $secondMatrix[150]
    //result must be  a 10x15 matrix as well (r1xc2)
    $resultMatrix[150]
.instructions
    #registerInitialization
	// LOAD VALUES OF MATRIX DIMENSIONS
	//r2 stores the VALUE of number matrix rows in firstMatrix
	loadv r2 $firstMatrixRows
	//r3 stores the value of number of matrix columns in firstMatrix
	loadv r3 $firstMatrixColumns
	//r4 stores the VALUE of number of rows in secondMatrix
	loadv r4 $secondMatrixRows
	//r5 stores the VALUE of the number of columns in the secondMatrix
	loadv r5 $secondMatrixColumns
	//r6 stores the VALUE of the number of rows in the resultMatrix
	loadv r6 $resultMatrixRows
	//r7 stores the VALUE of the number of columns in the resultMatrix
	loadv r7 $resultMatrixColumns

	// LOAD POINTERS TO MATRIX AND LOCKS
	
	//Make r1 stores the lock pointer
        load r1 $lock
	//load r9 with address to firstMatrix head
	load r9 $firstMatrix
	//load r10 with address to secondMatrix head
	load r10 $secondMatrix
	//load r11 with address to result matrix
  	load r11 $resultMatrix 
	//load r12 with ponter to doneRowValue 
	
	//return
	ret
	

#startMultiply
jal #registerInitialization
addi r0 r30 0x4
//this will execute: r29 = currRow *source1Cols
#columnLoop
//load address of currCol into r22
load r22 $currCol
//load value of currCol into r23
lw r22 r23

    #rowLoop
    //lets set r13 to  be the counter (set it to zero)
    add r0 r0 r13
    //r14 will be the accumulator for the multiply (set it to zero)
    add r0 r0 r14

    //load address of currRow into r20
    load r20 $currRow
    //load value of currRow into r21
    lw r20 r21

        #innerRowLoop
            jal #putSource1ValueIntoR29
            jal #putSource2ValueIntoR28
            //multiply the row * column store result in r28
            mul r28 r29 r28
            //accumultate the result from the multiply into r14
            add r28 r14 r14
            //increment counter
            addi r13 r13 0x1
            beq r13 r2 #writeValueToDestination
            beq r13 r13 #innerRowLoop

        #writeValueToDestination
            //write the value back
             jal #writeDestinationToMatrix
            
        //increment currRow
        addi r21 r21 0x1
        //store the new currRow value back to memory
        sw r20 r21
        //if the currentrow == the total number of rows in the source1 matrix branch to columnLoop
        beq r21 r2 #checkColumnLoop
        //otherwise loop back to the rowLoop
        beq r20 r20 #rowLoop

        #checkColumnLoop
            //set the currentRow value to 0
            sw r20 r0
            //increment currColumn
            addi r23 r23 0x1
            //store the incremented value back to memory
            sw r22 r23
            //if currCol == source2Cols then loop back to columnLoop
            beq r23 r5 #done
            //else go back to the rowLoop
            beq r23 r23 #columnLoop

#done
    halt
            

    #lock
        //compare M[$lock] to 0 if it is 0 then swap in the value of 1
        cas r1 r0 r5
        //If r5 and r0 are not equal then continue spinning
        beq r5 r0 #jumpBack
        beq r5 r5 #lock
    #jumpBack
        ret
    #unlock
        //set M[$lock] to 0
        sw r1 r0
        ret
#putSource1ValueIntoR29
            mul r21 r3 r29
            //add r29 to counter and store in r29
            add r29 r13 r29
            // multiply this by four to get the byte offset
            mul r29 r30 r29
            //load the word into r29 this is the value of the row
            lw r29 r29
            ret

#putSource2ValueIntoR28
            mul r13 r5 28
            //add r29 to counter and store in r28
            add r28 r23 r28
            // multiply this by four to get the byte offset
            mul r28 r30 r28
            //load the word into r28 this is the value of the row
            lw r28 r28
            ret

#writeDestinationToMatrix
    //curRow * destColumns in r28
    mul r20 r7 r28
    //(currRow*destColumns) + currCol in r28
    add r28 r23 r28
    //get ByteOffset
    mul r28 r30 r28
    //store the word back
    sw r28 r14
    ret
.startpc
#startMultiply
