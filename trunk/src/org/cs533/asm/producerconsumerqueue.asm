//This file is being used to test a producer consumer scenario
// using a single lock queue

.data
    // this is the lock variable. It is locked with value 1 and unlocked with value 0
    $lock 0
    $headOfQueue 0
    $tailOfQueue 0
.instructions

    #registerInitialization
        //Make r1 stores the lock pointer
        lui r1 U$lock
        ori r1 r1 L$lock

        //r2 stores the pointer to our heap
        lui r2 U$heap
        ori r2 r2 L$heap


        //r3 stores the head pointer of the queue
        lui r3 U$headOfQueue
        ori r3 r3 L$headOfQueue

        //r4 stores the tail pointer of the queue
        lui r4 U$tailOfQueue
        ori r4 r4 L$tailOfQueue

        //r5 stores the value of 1
        lui r5 U0x1
        ori r5 r5 L0x1

        //r6 holds an incrementer for the producers (to help provide values to store
        lui r6 U0x0
        ori r6 r6 L0x0

// r7 through r9 are reserved for global registers
        // r7 holds a return register

        //r8
        //r9

        //return to caller
        jr r7

    #producerInitialization
        jal #registerInitialization
        //we will set the head of the queue to be in the heap here M[$headOfQueue] = $heap
        sw r3 r2
        // now store memory[$tailOfQueue] to the same value (this is the empty queue)
        //set tail to the same spot as head and store in r4
        sw r4 r2
        beq r3 r3 #startProducing

    #consumerInitialization
    //set the initial register values
    jal #registerInitialization
        #headIsConstructed
            // now we load will load headOfQueue into r3
            lui r3 U$headOfQueue
            ori r3 r3 L$headOfQueue
            //if headOfQueue is still 0 then it has not been initialized..spin back
            beq r3 r0 #headIsConstructed

        #tailIsConstructedLoop
            //check to see if tail is constructed yet
            lui r4 U$tailOfQueue
            ori r4 r4 L$tailOfQueue
            beq r4 r0 #tailIsConstructedLoop
            //once this is done we are ready to go
            beq r3 r3 #startConsuming


    #startConsuming
        //get the lock
        jal #lock
        //load pointer to head into r10
        lw r3 r10
        //load pointer to tail into r12
        lw r4 r12
        // if head==tail then the queue is empty so we jump to the emptyQueue label
        beq r10 r12 #emptyQueue
        // add head to 4 to get the next location
        addi r10 r10 0x4
        //get memory[r10] into r11 (this is the value pointed to by head
        lw r10 r11
        //store new head value
        sw r3 r10
        //unlock
        jal #unlock
        //go back through the loop
        beq r3 r3 #startConsuming

    #emptyQueue
        jal #unlock
        beq r3 r3 #startConsuming


    #startProducing
        //get the lock
        jal #lock
        //load pointer to tail into r10
        lw r4 r10
        // increment the tail by 4
        addi r10 r10 0x4
        //store the counter (r6) into the value pointed to by tail
        sw r10 r6
        //increment counter
        addi r6 r6 0x1
        //store new tail value
        sw r4 r10
        jal #unlock
        beq r3 r3 #startProducing


    #lock
        //compare M[$lock] to 0 if it is 0 then swap in the value of 1
        cas r1 r0 r5
        //If r5 and r0 are not equal then continue spinning
        beq r5 r0 #jumpBack
        beq r5 r5 #lock
    #jumpBack
        jr r7

    #unlock
        //set M[$lock] to 0
        sw r1 r0
        jr r7


.startpc
    #producerInitialization
    #consumerInitialization
    #consumerInitialization   