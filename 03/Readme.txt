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
transforms). In the example of objects, the name from the xml file get matched to the .obj files in
the models package.  They are used to obtain the appropriate Polygon meshses using ObjImporter and
ultimately added ot the scenegraph.  When adding to the scenegraph, a map is used to match the name
to the mesh for efficient lookup.

The transforms are also parsed into their own separate nodes, appropriately named TransformNode.
These transformnodes are the result of parsing the xml files for the type of transformation (scale,
rotate, translate), as well as their values to save the respective matrix transformation.

More plainly put, the scenegraph contains the nodes for both the objects, as well as separate nodes
of transforms that will be applied to the subtrees within the overall scenegraph. This allows
transforms to be applied to the ever-evolving perception of what we consider an object (e.g. a fan,
a spinning fan, an oscillating fan, etc. as seen in class).

=========================================

2. The scene graph is drawn by first being initialized in the View class, which sets off the process
of parsing the xml file as described in #1.  The view class also initializes the renderer
and passes it to the scenegraph.  When the view class calls the draw method, it is passed
to the scene graph, who passes it to the renderer class (GL3ScenegraphRenderer).  As stated
in the end of #1, by placing the transformnodes in the appropriate place in the scenegraph, the
transformations are applied to all of the subgraphs/subtrees below that node.  It does this by
managing what is being drawn through a stack.  As leafnodes are drawn, they are popped from the
stack but the transformations are not until all of the subgraphs have been rendered. This process
is further described in #3.  In the XML file, we are structuring the transformations next to the
object/composite objects that we can the transformation applied to (with the <set> tags. The same
idea is applied when building the scenegraph to make sure the transofmrations are applied correctly.

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


