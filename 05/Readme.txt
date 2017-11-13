Krishna V Jayanthi
Arnold Esguerra
5310 Computer Graphics
Fall 2017
Assignment 5

====================================================

Program features:

====================================================

All the features of assignment 4 still work in this assignment.

There are 3 rooms:
One having the table with a pyramid, a mace, Thor's hammer circling the table and a flying machine (a poor emulation of one of DaVinci's designs)
Second room has a copy of the hammer, spinning vertically on the floor.
Third room room has 3 maces leaning against one wall in the room.

All animations are again controlled by xml attributes like before, using the <animationType> tag instead of hard coding the values.

There are 9 lights as before, all of which can be switched on/off with the number keys on the keyboard.

====================================================

1. Textures:
All the objects in the scene have textures attached to them. These textures are applied through scenegraph xml.
All objects now have a <textureName> tag that specifies the texture name to be applied to the model.

There are a bunch of textures used in this assignment.
Walls and floor are wooden, the corridor has a different wodden wall.
The table is made up of a wooden base and different wodden legs.
The mace has a golden texture applied to the sphere, a steel texture on it's axil. the hilt of the mace is left without textures with the right colors applied to the material.
The hammer object has two metalic textures to the hammer's head. Leather to the axil and the hilt has steel texture applied. The small circle on the top of the hammer is left without textures.

If no texture is applied through the <textureName></textureName> tags, the renderer picks up white.png as the default texture and applies it to the model.
The pyramid object is such an example, which does not have any textures applied through the scenegraph xml.
The pyramid has specefic ambient, diffuse and specular colors applied to it. This is what gives the object color.

====================================================

2. Exploded View:

The exploded view is implemented in this assignment. It's controlled through the scenegraph xml and is not hard coded.
In effect, every group tag that contains the attribute 'showExploded="true"' will be exploded when the user presses the E key. With this, we can control the level of detail 
we want to explode. In this assignment, the davinci's machine used has 2 gears a platform and a chopper wings. If we were to explode the entire thing down to each leaf node, the entire
output would look messy.

Using this attribute, we can control the level of detail, so that the gears are not further exploded (each gear is a composite object of a bunch of shapes stuck together).
When the user presses the 'E' key, he can see the exploded view for the machine. This should seperate the platform, the humanoid, the vertical shafts, the gears with their axils, 
the pillars, the tilted beams on the top, the chopper blades (attached together). Each gear and it's shaft is seen exploded together to clearly show the working of the machine.
The humanoid waving on the main platform is also shifted aside. His structure is not further exploded.

====================================================

3. Program setup:

The program when run, starts with the camera in the center of the corridor, facing the room containing the table and other objects on it.
By pressing the up arrow key, you can fly towards the room. The machine that 'explodes' is flying in the room, slightly above the table.

Pressing the 'E' key will explode it, keeping the animation intact. the exploded machine will still move as per the animation.
This makees it easy to visualize the parts that make up the whole.

As before, you can turn around and head to the other two rooms as well, control the lights with the numnber keys to turn them on/off.

====================================================

3. Animation Video:

Here's the link to the animation video:
https://github.com/ajesguerra/webdev-esguerra-arnold/blob/master/src/assets/projectimages/outassignment5.mp4

