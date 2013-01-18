/**
Jaivox applications have three parts: a speech recognizer, an interpreter
and a speech synthesizer. The recognizer and synthesizer are made by others.
In our typical application, the recognizer is Sphinx from CMU and the
synthesizer is Festival from Edinburgh. These components can be replaced
with other solutions. For example, the recognizer can be a commercial product
like Nuance's Dragon, the synthesizer also may be a commercial product such
as from Nuance or Microsoft.

The interpreter is the main component created by Jaivox. This component
can deal with some of the errors in the recognition. It also deals with
specific subjects and tries to produce succinct answers to spoken questions.

The interpreter normally deals with a qualitative database. But it can also
be used to connect to external applications and external databases.

The interpreter package has three classes associated with turning the
interpreter into an agent. These are InterServer, InterSession and
InterResponder. The main interpretive work is handled in Interact. The
answers are formulated in Script. There are a number of other classes to
deal with managing the conversation and creating appropriate answers.
*/

package com.jaivox.interpreter;
