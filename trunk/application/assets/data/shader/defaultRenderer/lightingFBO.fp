#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;

uniform float intensity;
uniform vec2 position;
uniform vec2 resolution;
uniform vec4 color;

void main()
{
  gl_FragColor = texture2D(u_texture, v_texCoords) + color * min(1.0, (intensity / distance(v_texCoords * resolution, position)));
}