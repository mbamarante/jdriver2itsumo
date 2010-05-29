// Agent boss in project delegate-tasks

/* Initial beliefs and rules */

/*rules*/

has_od(S,G) :- 
	start(S) & goal(G) &
	S > 0 & G > 0.

an_agent(Ag) :-
   .all_names(L) & .length(L,X) & .random(R,0) &
   .nth(R*X,L,Ag) & .substring("driver", Ag). /*1+ = evitar agente zero*/

/*beliefs*/

status(parked). /*[parked,start_engine,driving]*/
/*decide(nothing).*/ /*[nothing,something]*/

start(0).
goal(0).

/* Initial goals */

/* !start_engine. */


/* Plans */

@ds1
+decide(something): status(parked) & has_od(S,G)
	<- 	-+status(start_engine); /* update belief base */
		.concat("trigger: requested action; context: status(parked) & has_od(S,G)", M);
		.print(M);
		!drive(S,G).
		
+decide(something): not status(parked) 
	<- jia.doNothing.

+!drive(S,G): status(start_engine)
	<-  -+status(driving);
		.concat("driving from ", S ," to ", G , M);
		.print(M);
		jia.driveOnPath(S,G).
		
+!drive(S,G): status(driving)
	<-	jia.doNothing.
/*
+!drive(S,G) : status(driving)
	<-	true.
*/