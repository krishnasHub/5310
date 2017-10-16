Krishna V Jayanthi
Arnold Esguerra
5310 Computer Graphics
Fall 2017
Assignment 3

=========================================

1.  The scene graph is made all of the objects that is being rendered to
the viewer and is organized in a tree-like structure.  It obtains the data for the structure
by the View class using the sgraph package to take in the XML file from
scenegraphmodels and parsing it appropriately into its respective PolygonMeshes.

This parsing is done in the SceneXMLReader class (MyHandler and SceneXMLReader nested classes).  During
parsing, elements are pushed onto a stack, matching how they are nested in the xml file.
The elements that get pushed are LeafNodes.  These leaves are parsed by matching the name from the xml
file to the .obj files in the models package. They are used to obtain the appropriate Polygon meshses using
ObjImporter and ultimately added ot the scenegraph.  When adding to the scenegraph, a map is used to match
the name to the mesh for efficient lookup.

Additionally, the transforms are also parsed into their own separate nodes, appropriately named TransformNode.
These transformnodes are the result of parsing the xml files for the type of transformation (scale, rotate,
translate), as well as their values to save the respective matrix transformation.

More plainly put, the scenegraph contains the nodes for both the objects, as well as separate nodes of transforms that will be applied to the subtrees within the overall scenegraph. This allows transforms to be applied to the ever-evolving perception of what we consider an object (e.g. a fan, a spinning fan, an oscillating fan, etc. as seen in class).

=========================================

2. The scene graph is drawn by first being initialized in the View class, which sets off the process
of parsing the xml file as described in #1.  The view class also initializes the renderer
and passes it to the scenegraph.  When the view class calls the draw method, it is passed
to the scene graph, who passes it to the renderer class (GL3ScenegraphRenderer).  As stated
in the end of #1, by placing the transformnodes in the appropriate place in the scenegraph, the transformations
are applied to all of the subgraphs/subtrees below that node.  In the XML file, we are structuring
the transformations next to the object/composite objects that we can the transformation applied to (with the
<set> tags. The same idea is applied when building the scenegraph to make sure the transofmrations are
applied correctly.

=========================================

3. The GL3SceneGraphRenderer class's job is ultimately to render every mesh in the scenegraph, as
implied by the name. This class encapsulates the gl context, the shader vertex map and (more importantly) the map of meshes to render.
In the SceneGraphRenderer class, when we set the renderer, we add all the meshes from the leaf nodes into the renderer. This way, the renderer only has the Meshes that actually have to be drawn.
When the draw function is called on the SceneGraphRenderer, it simply calls the draw on teh rootNode. Since every Node set in the SceneGraph can either be a GroupNode or a LeafNode, the draw function of the GroupNode in turn calls all it's children's draw method until a LeafNodei s called.
The LeafNode internally calls the SceneGraphRenderer's drawMesh function, which points to the actual Mesh we want to draw on the screen.
This draw function - drawMesh uses the transformation (which is provided by teh SceneGraph) to actually draw the mesh on the screen.

To extend this further, if I want to write a textual descrption of each node, I would make changes in the SceneGraph class and to the  SceneGraphRenderer class (and it's interface) to add a method called renderText() and implement the actual OpenGL code to write Text on  screen in the SceneGraphRenderer class.
I would further add this renderText() contract to the ISceneGraphRenderer interface as well.
The SceneGraph class would hold the textual information to be rendered - the name of the node, the type of the node (Group Node or Leaf node) and any more textual information we want to render about this node.
This information can reside at the Node level so that each node has it's own text to be rendered.
Then, in the GroupNode's draw method, we can call the context's renderText() function first before calling the draw() method.
This way, every node's textual information is rendered before the node is rendered.

