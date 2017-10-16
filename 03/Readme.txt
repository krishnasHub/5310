Krishna V Jayanthi
Arnold Esguerra
5310 Computer Graphics
Fall 2017
Assignment 3

=========================================

RUNNING THE PROGRAM & COMPLETION
Both the YMCA and three rooms were completed in this assignment.  In order to run the program,
you can just run the ScenegraphViewer class.  The rooms rendering is seen first when you load the
program.  It starts off in the hallway, but faces the room with the table and objects on top
so it can be seen from the current position.  By looking left and right (left arrow and right
arrow keys), you will see the 2nd and third rooms (that are empty). You can use the "w" and "s" keys
to nod up and down, as well as the up/down arrow keys to move positions as instructed.

Both the YMCA and three rooms are rendered upon loading the program.  When you press the "y" and
"r" arrow keys, you can switch between scenes.  The scene graphs do not need their xml files to be
parsed as this was done in the beginning.

As you move through the rooms, at any point you can switch to the YMCA scene.  As you switch,
the position and where you are looking at in the rooms is saved.  But, when you enter the YMCA
scene you always are at the same starting position.  This is intentional as when you move around the
room scene, your eye position and lookAt will be applied to the YMCA scene.  By moving around the
room, you may have the YMCA humanoids become out of view and difficult to find, that is why
it is always reset to the same starting position.  You can move and have the same functionality
in the YMCA scene, but when you switch back to the room, you will be at the same position as
before you went to the YMCA scene (no matter where you move in the YMCA scene).  

=========================================

1.  The scene graph is made all of the objects that is being rendered to
the viewer and is organized in a tree-like structure.  It obtains the data for the structure
by the View class using the sgraph package to take in the XML file from
scenegraphmodels and parsing it appropriately into its respective PolygonMeshes.

This parsing is done in the SceneXMLReader class (MyHandler and SceneXMLReader nested classes).
During parsing, elements are pushed onto a stack, matching how they are nested in the xml file.
The elements that get pushed are INodes.  These nodes are parsed by matching the tag (objects or
transforms). In the example of objects, the name from the xml file get matched to the .obj files
in the models package.  They are used to obtain the appropriate Polygon meshses using ObjImporter
and ultimately added ot the scenegraph.  When adding to the scenegraph, a map is used to match the
name to the mesh for efficient lookup.

The transforms are also parsed into their own separate nodes, appropriately named TransformNode.
These transformnodes are the result of parsing the xml files for the type of transformation (scale,
rotate, translate), as well as their values to save the respective matrix transformation.

More plainly put, the scenegraph is basically a tree structure that holds nodes. These nodes are
one of the three types:
1. GroupNode - These group a set of Nodes to represent a collection (or group) of objects that
behave cumulatively in a particular fashion and can be logically grouped together. A good example
would be hand, wrist, forearm and arm all being part of the group Right Arm. (another example
would be the fan: a fan, a spinning fan, an oscillating fan, etc. as seen in class).
2. TransformNode - These are nodes that hold the transformations to be applied to this node and
all the nodes that branch from it. This is useful if you want to apply a transformation to all
the child nodes of a group so they all behave in a particular way. An example would be rotating
the right arm by 10 degrees, which would imply rotating all the parts of the arm by the same
angle, or moving the arm by 20 pixels and moving all the parts of the arm by the same amount.
3. LeafNode - These are the nodes that represent the actual individual meshes that represent,
either a part of the main object or the entire object in itself. These are the actual polygon
meshes we want to draw on the screen. As examples, these are the entire right forearm, a sphere
or anything more complex.

The way we use this data structure is useful, in that, with the current organization, we can
easily traverse the tree to get to that block (or group) of meshes and apply a transformation
to the entire structure, just by adding a transformation to that node. This makes applying
transformations very easy and intuitive. Working on this tree is simply implementing the
Preorder tree traversal when it comes to drawing the entire set, starting from the root node or
any other node of interest.

=========================================

2. The scene graph is drawn by first being initialized in the View class, which sets off
the process of parsing the xml file as described in #1 to get the Scenegraph data structure
that's basically a well designed tree.

This tree (as explained above) has three different nodes, from which the TransformNode is the
one that holds the transformations we want to apply to the group stored in the children of the
node. The View class passes the modelView that has the trackball transformations and the camera
placement applied to it. The root node (which is a group node) of the Scenegraph grabs it
passes it down to all it's children, calling the draw method on each.

The TransformNode, when the draw function is called on it, with the ModelView stack passed,
first pushes the copy of the top most transformation into the modelView stack, making sure
the latest transformation is present. It then applies it's own set of transforms to this
modelview (the one on the top of the stack) and calls the draw on all it's children.
This way, when the children are called, if a child is a LeafNode, it's drawn with the current
set of transformations from the modelView that was set by the TransformNode. If the child is
a group node or another transform node, we add more transformations to the modelView stack.
Once all the children are rendered, we pop the transformation from the modelView stack.
This way, when this node is 'done' being drawn, the next node gets a free transformation
from the top of the stack, which is unrelated to the current transformations.

This traversal is analogous to PreOrder tree traversal where we first work on the
current node, then the child nodes.

The obvious advantage in using the scenegraph data structure comes by placing the
transformnodes in the appropriate place in the scenegraph, the transformations are applied
to all of the subgraphs/subtrees below that node. It does this by managing what is being
drawn through a stack.  As leafnodes are drawn, they are popped from the
stack but the transformations are not until all of the subgraphs have been rendered.

In the XML file, we are structuring the transformations next to the object/composite
objects that we can the transformation applied to (with the <set> tags.
The same idea is applied when building the scenegraph to make sure the transformations are
applied correctly.

=========================================

3. The GL3SceneGraphRenderer class's job is ultimately to render every mesh in the scenegraph, as
implied by the name. This class encapsulates the gl context, the shader vertex map and (more
importantly) the map of meshes to render. In the SceneGraphRenderer class, when we set the renderer,
we add all the meshes from the leaf nodes into the renderer.

When the draw function is called on the SceneGraphRenderer, it simply calls draw on the rootNode.
Since every Node set in the SceneGraph can either be a GroupNode, TransformNode or a LeafNode:
1. The draw function of the GroupNode in turn calls all it's children's draw method.
2. The TransformNode internally just applies the transformations (and animations) to the modelView
and then calls the draw on it's children;
3. The LeafNode internally calls the SceneGraphRenderer's drawMesh function, which points to the
actual Mesh we want to draw on the screen and draws it calling OpenGL calls (internally calling the
ObjectInstance class holding the mesh)

The point of using this design, vs drawing all the meshes one by one is to use the right
transformations set according to parent node of the graph. Once we begin to draw a node and all it's
children, we set the transformations on the modelView matrix using the TransformNode. Then, we draw
all the children which can be TransformNodes or LeafNodes or GroupNodes. When they are drawn with
their transformations, we then pop out the current transform from the modelView matrix. This way,
all the transformations applied to this node are applied to all the child nodes. This design is
slightly analogous to the preorder tree traversal algorithm applied to graphs.

Text Rendering.

Rendering text on the screen can be interpreted as two things:

1. Seeing Text on the world as 3D objects.
For this, the text we want to see can be rendered as Objects on the world. One way to do that would
be to use textures that only have the text we want to print. We can apply these textures to simple
meshes and add them to the scenegraph. Finally, we can transform these meshes so that we can place
them in the 3D world by scaling, translating and rotating them to see them as we want. This way,
there isn't much framework change, except using the addTexture() functions to add the textures to
the right meshes at the right place in the graph.

2. Seeing text on the screen like log messages, similar to the frame rate info we see on the screen.
For this, I would make changes in the SceneGraph class and to the  SceneGraphRenderer class (and
it's interface) to add a method called renderText() and implement the actual OpenGL code to write
Text on  screen in the SceneGraphRenderer class. I would further add this renderText() contract to
the ISceneGraphRenderer interface as well. The SceneGraph class would hold the textual information
to be rendered - the name of the node, the type of the node (Group Node or Leaf node) and any more
textual information we want to render about this node. This information can reside at the Node level
so that each node has it's own text to be rendered. Then, in the GroupNode's draw method, we can
call the context's renderText() function first before calling the draw() method. This way, every
node's textual information is rendered before the node is rendered. This is again analogous to the
preorder tree traversal algorithm, where we print out the node, then go for the child nodes to print
out their information on the screen, as we build each node.


