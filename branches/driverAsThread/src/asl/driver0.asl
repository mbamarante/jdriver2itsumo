// Agent worker in project delegate-tasks

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true <- .send(proxy, achieve, connect).

/*
@d1
+!do(TaskNumber)[source(proxy)]
   :  .my_name(Me) & not completedTask(TaskNumber)
   <- 
      doTask(TaskNumber);
      .concat("waitingCar",M1); 
	  .send(proxy,tell,msg(M1)).

@d2
+!do(TaskNumber)[source(proxy)]
   :  .my_name(Me) & completedTask(TaskNumber)
   <- .concat("task ", TaskNumber, " already done!", M);
   	  .print(M).
   	  */