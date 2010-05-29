// Agent boss in project delegate-tasks

/* Initial beliefs and rules */

/*rules*/

has_od :- 
	start(S) &
	goal(G) &
	S > 0 & 
	G > 0.

an_agent(Ag) :-
   .all_names(L) & .length(L,X) & .random(R,0) &
   .nth(R*X,L,Ag) & .substring("driver", Ag). /*1+ = evitar agente zero*/

/*beliefs*/

driving(0).
start(0).
goal(0).
actionRequested(0).


/* Initial goals */

!start_engine.


/* Plans */

@d1
+!start_engine : driving(D) & D==0 & has_od & start(S) & goal(G) & actionRequested(A) & A>0
	<-  .concat("not driving and [O,D] is [",S,",",G,"] -- start engine!", M);
		.print(M);
		!drive(S,G).
		
+!start_engine : actionRequested(A) & A==0 
	<-  /*.concat("no action!", M);
		.print(M);*/
		!!start_engine.
		
+!start_engine : driving(D) & D==0
	<-  /*.concat("no action!", M);
		.print(M);*/
		!!start_engine.
	
+!drive(S,G) : driving(D) & D==0
	<-  -+driving(1); /* update belief base */
		.concat("driving from ", S ," to ", G , M);
		.print(M);
		-start_engine;
		jia.driveOnPath(S,G).

+!drive(S,G) :driving(D) & D>0
	<-	true.

/*@c1
+!connect[source(D)]
	: true <-
	.print(D);
	jia.proxyConnectDriver(D).
*/