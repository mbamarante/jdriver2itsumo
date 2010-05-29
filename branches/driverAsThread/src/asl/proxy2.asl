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
decide(nothing). /*[nothing,something]*/

start(0).
goal(0).

/* Initial goals */

/* !start_engine. */


/* Plans */

@d1
+!start_engine : status(parked) & decide(something) & has_od(S,G)
	<-  .concat("not driving and [O,D] is [",S,",",G,"] -- start engine!", M);
		.print(M);
		!drive(S,G).
		
+!start_engine : status(parked) & decide(nothing)
	<-  /*.concat("no action!", M);
		.print(M);*/
		!!start_engine.

/*		
+!start_engine : status(parked)
	<- !!start_engine.*/

+!drive(S,G) : status(parked)
	<-  -+status(driving); /* update belief base */
		.concat("driving from ", S ," to ", G , M);
		.print(M);
		-start_engine;
		jia.driveOnPath(S,G).

+!drive(S,G) : status(driving)
	<-	true.

/*@c1
+!connect[source(D)]
	: true <-
	.print(D);
	jia.proxyConnectDriver(D).
*/