#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform sampler2D occlusion;
uniform sampler2D occlusion2;
uniform vec4 dayLightColor;

void main()
{
  vec2 inverted = vec2(v_texCoords.x, 1.0 - v_texCoords.y);
  float factor1 = texture2D(occlusion, inverted).r;
  float factor2 = texture2D(occlusion2, inverted).r;
  float factor = factor1 * factor2;
  gl_FragColor = texture2D(u_texture, inverted) * vec4(factor, factor, factor, 1.0) * dayLightColor;
}